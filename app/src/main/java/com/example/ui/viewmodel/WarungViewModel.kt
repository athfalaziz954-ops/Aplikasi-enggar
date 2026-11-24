package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.CartItemPayload
import com.example.data.local.CustomerEntity
import com.example.data.local.KasbonEntryEntity
import com.example.data.local.ProductEntity
import com.example.data.local.SettingsDataStore
import com.example.data.local.StoreSettings
import com.example.data.local.TransactionEntity
import com.example.data.local.WarungDatabase
import com.example.data.repository.AiAssistantRepository
import com.example.data.repository.CsvExportType
import com.example.data.repository.NetworkMonitor
import com.example.data.repository.WarungRepository
import com.example.util.CurrencyAndDateUtils
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface AiUiState {
    data object Idle : AiUiState
    data class Loading(val actionLabel: String) : AiUiState
    data class Streaming(val actionLabel: String, val text: String) : AiUiState
    data class Success(val actionLabel: String, val text: String) : AiUiState
    data class Error(val message: String) : AiUiState
}

data class ToastEvent(
    val id: Long = System.currentTimeMillis(),
    val message: String,
    val isError: Boolean = false
)

class WarungViewModel(application: Application) : AndroidViewModel(application) {

    private val database = WarungDatabase.getInstance(application)
    private val repository = WarungRepository(database)
    private val settingsStore = SettingsDataStore(application)
    private val networkMonitor = NetworkMonitor(application)
    private val aiRepository = AiAssistantRepository()

    val products: StateFlow<List<ProductEntity>> = repository.productsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<TransactionEntity>> = repository.transactionsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customers: StateFlow<List<CustomerEntity>> = repository.customersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val kasbonEntries: StateFlow<List<KasbonEntryEntity>> = repository.kasbonEntriesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<StoreSettings> = settingsStore.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StoreSettings())

    val isSystemOnline: StateFlow<Boolean> = networkMonitor.isOnlineFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isEffectivelyOnline: StateFlow<Boolean> = combine(isSystemOnline, settings) { sysOnline, st ->
        sysOnline && !st.simulateOffline
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val pendingSyncCount: StateFlow<Int> = combine(
        products,
        transactions,
        kasbonEntries
    ) { prods, txs, kasbons ->
        prods.count { !it.isSynced } + txs.count { !it.isSynced } + kasbons.count { !it.isSynced }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _cartQuantities = MutableStateFlow<Map<Long, Int>>(emptyMap())
    val cartQuantities: StateFlow<Map<Long, Int>> = _cartQuantities.asStateFlow()

    val cartItems: StateFlow<List<CartItemPayload>> = combine(products, _cartQuantities) { prods, qtyMap ->
        val prodById = prods.associateBy { it.id }
        qtyMap.mapNotNull { (id, qty) ->
            val p = prodById[id]
            if (p != null && qty > 0) {
                CartItemPayload(
                    productId = p.id,
                    name = p.name,
                    barcode = p.barcode,
                    costPrice = p.costPrice,
                    sellPrice = p.sellPrice,
                    quantity = qty
                )
            } else null
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cartTotal: StateFlow<Long> = combine(cartItems) { arr ->
        arr.firstOrNull()?.sumOf { it.subtotal } ?: 0L
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val cartItemCount: StateFlow<Int> = combine(cartItems) { arr ->
        arr.firstOrNull()?.sumOf { it.quantity } ?: 0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _toastEvent = MutableStateFlow<ToastEvent?>(null)
    val toastEvent: StateFlow<ToastEvent?> = _toastEvent.asStateFlow()

    private val _latestReceipt = MutableStateFlow<TransactionEntity?>(null)
    val latestReceipt: StateFlow<TransactionEntity?> = _latestReceipt.asStateFlow()

    private val _flashProductId = MutableStateFlow<Long?>(null)
    val flashProductId: StateFlow<Long?> = _flashProductId.asStateFlow()

    private val _aiState = MutableStateFlow<AiUiState>(AiUiState.Idle)
    val aiState: StateFlow<AiUiState> = _aiState.asStateFlow()

    private var toastJob: Job? = null
    private var aiJob: Job? = null

    init {
        viewModelScope.launch {
            repository.ensureInitialProductsSeeded()
        }
    }

    fun showToast(message: String, isError: Boolean = false) {
        toastJob?.cancel()
        _toastEvent.value = ToastEvent(message = message, isError = isError)
        toastJob = viewModelScope.launch {
            delay(3600L)
            _toastEvent.value = null
        }
    }

    fun dismissToast() {
        toastJob?.cancel()
        _toastEvent.value = null
    }

    fun addToCart(product: ProductEntity) {
        if (product.stock <= 0) {
            showToast("Stok ${product.name} sedang habis!", isError = true)
            return
        }
        val current = _cartQuantities.value.toMutableMap()
        val existingQty = current[product.id] ?: 0
        if (existingQty >= product.stock) {
            showToast("Maksimal sesuai sisa stok (${product.stock})", isError = true)
            return
        }
        current[product.id] = existingQty + 1
        _cartQuantities.value = current
        triggerProductFlash(product.id)
    }

    fun updateCartQuantity(productId: Long, newQty: Int) {
        val current = _cartQuantities.value.toMutableMap()
        if (newQty <= 0) {
            current.remove(productId)
        } else {
            val product = products.value.find { it.id == productId }
            val maxStock = product?.stock ?: newQty
            if (newQty > maxStock) {
                showToast("Stok hanya tersisa $maxStock", isError = true)
                current[productId] = maxStock
            } else {
                current[productId] = newQty
            }
        }
        _cartQuantities.value = current
    }

    fun clearCart() {
        _cartQuantities.value = emptyMap()
    }

    private fun triggerProductFlash(productId: Long) {
        viewModelScope.launch {
            _flashProductId.value = productId
            delay(450L)
            if (_flashProductId.value == productId) {
                _flashProductId.value = null
            }
        }
    }

    fun processScannedBarcode(rawBarcode: String, onFound: (ProductEntity) -> Unit, onNotFound: (String) -> Unit) {
        val code = rawBarcode.trim()
        if (code.isBlank()) return
        viewModelScope.launch {
            val match = repository.findProductByBarcode(code)
            if (match != null) {
                CurrencyAndDateUtils.playBeepSound()
                addToCart(match)
                showToast("Ditambahkan: ${match.name}")
                onFound(match)
            } else {
                showToast("Barcode $code belum terdaftar di stok", isError = true)
                onNotFound(code)
            }
        }
    }

    fun simulateRandomBarcodeScan(): ProductEntity? {
        val available = products.value.filter { it.stock > 0 }
        if (available.isEmpty()) {
            showToast("Semua stok produk habis!", isError = true)
            return null
        }
        val chosen = available.random()
        CurrencyAndDateUtils.playBeepSound()
        addToCart(chosen)
        showToast("Scan simulasi: ${chosen.name} (${chosen.barcode})")
        return chosen
    }

    fun completeCheckout(
        paymentMethod: String,
        paidAmount: Long,
        customerId: Long?,
        newCustomerName: String?,
        newCustomerWhatsapp: String?
    ) {
        val items = cartItems.value
        if (items.isEmpty()) {
            showToast("Keranjang masih kosong!", isError = true)
            return
        }
        viewModelScope.launch {
            val tx = repository.checkoutTransaction(
                cartItems = items,
                paymentMethod = paymentMethod,
                paidAmount = paidAmount,
                customerId = customerId,
                customerNameInput = newCustomerName,
                customerWhatsappInput = newCustomerWhatsapp
            )
            _cartQuantities.value = emptyMap()
            _latestReceipt.value = tx
            showToast(
                if (isEffectivelyOnline.value) {
                    "Transaksi ${tx.invoiceNumber} berhasil disimpan!"
                } else {
                    "Tersimpan lokal (Offline): ${tx.invoiceNumber}"
                }
            )
        }
    }

    fun dismissReceiptModal() {
        _latestReceipt.value = null
    }

    fun saveProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.saveProduct(product)
            showToast("Produk \"${product.name}\" disimpan")
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            val current = _cartQuantities.value.toMutableMap()
            current.remove(product.id)
            _cartQuantities.value = current
            showToast("Produk \"${product.name}\" dihapus")
        }
    }

    fun adjustStock(productId: Long, delta: Int) {
        viewModelScope.launch {
            repository.adjustProductStock(productId, delta)
        }
    }

    fun addCustomer(name: String, whatsapp: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.addCustomer(name, whatsapp)
            showToast("Pelanggan \"$name\" ditambahkan")
        }
    }

    fun recordKasbonInstallment(customerId: Long, amount: Long, note: String) {
        viewModelScope.launch {
            repository.recordKasbonInstallment(customerId, amount, note)
            showToast("Cicilan ${CurrencyAndDateUtils.formatRupiah(amount)} dicatat!")
        }
    }

    fun toggleOfflineSimulation() {
        viewModelScope.launch {
            val nextOffline = !settings.value.simulateOffline
            settingsStore.setSimulateOffline(nextOffline)
            if (!nextOffline && isSystemOnline.value) {
                showToast("Mode Online aktif")
                if (pendingSyncCount.value > 0) {
                    syncNow()
                }
            } else if (nextOffline) {
                showToast("Mode Simulasi Offline aktif • Data aman tersimpan lokal")
            }
        }
    }

    fun syncNow() {
        if (_isSyncing.value) return
        if (!isEffectivelyOnline.value) {
            showToast("Sedang Offline. Aktifkan mode Online untuk sinkronisasi.", isError = true)
            return
        }
        viewModelScope.launch {
            _isSyncing.value = true
            val result = repository.syncToGoogleSheets(settings.value)
            if (result.success) {
                settingsStore.updateLastSyncTime()
            }
            _isSyncing.value = false
            showToast(result.message, isError = !result.success)
        }
    }

    fun updateStoreSettings(
        storeName: String,
        storeAddress: String,
        storeWhatsapp: String,
        invoiceFooter: String,
        webhookUrl: String
    ) {
        viewModelScope.launch {
            settingsStore.updateStoreProfile(
                storeName = storeName,
                storeAddress = storeAddress,
                storeWhatsapp = storeWhatsapp,
                invoiceFooter = invoiceFooter,
                webhookUrl = webhookUrl
            )
            showToast("Pengaturan warung & Google Sheets disimpan")
        }
    }

    suspend fun generateCsv(type: CsvExportType): String {
        return repository.buildCsvContent(type)
    }

    fun importProductsFromCsv(csvText: String) {
        viewModelScope.launch {
            val count = repository.importProductsFromCsv(csvText)
            if (count > 0) {
                showToast("Berhasil mengimpor $count produk dari CSV!")
            } else {
                showToast("Format CSV tidak valid atau kosong", isError = true)
            }
        }
    }

    // --- Gemini AI UMKM Assistant ---

    fun askAiKulakan() {
        val lowStock = products.value.filter { it.isLowStock }
        val allProds = products.value
        val stockContext = if (lowStock.isNotEmpty()) {
            lowStock.joinToString("\n") { p ->
                "- ${p.name}: sisa ${p.stock} (batas min ${p.minStock}), harga modal ${CurrencyAndDateUtils.formatRupiah(p.costPrice)}"
            }
        } else {
            allProds.sortedBy { it.stock }.take(5).joinToString("\n") { p ->
                "- ${p.name}: sisa ${p.stock} (batas min ${p.minStock}), harga modal ${CurrencyAndDateUtils.formatRupiah(p.costPrice)}"
            }
        }

        val prompt = """
            Buatkan daftar belanja kulakan ke agen/distributor untuk ${settings.value.storeName}.
            Berikut data produk yang stoknya menipis / perlu dipantau:
            $stockContext

            Susun format daftar kulakan yang rapi, mudah disalin untuk dikirim ke distributor via WhatsApp, lengkap dengan saran jumlah kulakan dan perkiraan total modal yang perlu disiapkan.
        """.trimIndent()

        runAiPrompt("Susun Daftar Kulakan", prompt)
    }

    fun askAiBillingMessage(customer: CustomerEntity?) {
        val target = customer ?: customers.value.firstOrNull { it.remainingDebt > 0L }
        val prompt = if (target != null) {
            val entries = kasbonEntries.value.filter { it.customerId == target.id }.take(5)
            val details = entries.joinToString("; ") { "${it.note} (${CurrencyAndDateUtils.formatRupiah(it.amount)})" }
            """
                Buatkan 2 variasi pesan pengingat tagihan kasbon WhatsApp yang sangat sopan, akrab, dan menghargai pelanggan dari "${settings.value.storeName}":
                1. Versi Ramah & Kekeluargaan
                2. Versi Singkat & Jelas
                Data pelanggan:
                - Nama: ${target.name}
                - Total Utang: ${CurrencyAndDateUtils.formatRupiah(target.totalDebt)}
                - Sudah Dibayar: ${CurrencyAndDateUtils.formatRupiah(target.paidDebt)}
                - Sisa Tagihan: ${CurrencyAndDateUtils.formatRupiah(target.remainingDebt)}
                - Catatan terakhir: ${details.ifBlank { "Belanja kebutuhan warung" }}
            """.trimIndent()
        } else {
            """
                Buatkan 2 template pesan pengingat kasbon WhatsApp yang sopan, hangat, dan kekeluargaan untuk pelanggan "${settings.value.storeName}", lengkap dengan placeholder [Nama Pelanggan] dan [Sisa Tagihan].
            """.trimIndent()
        }
        runAiPrompt("Buat Pesan Tagihan Ramah", prompt)
    }

    fun askAiDailyReport() {
        val startOfDay = CurrencyAndDateUtils.startOfTodayMillis()
        val todayTx = transactions.value.filter { it.timestamp >= startOfDay }
        val omzet = todayTx.sumOf { it.totalAmount }
        val profit = todayTx.sumOf { it.estimatedProfit }
        val cashCount = todayTx.count { it.paymentMethod == "TUNAI" }
        val kasbonTotal = todayTx.filter { it.paymentMethod == "KASBON" }.sumOf { it.changeOrRemaining }

        val topProducts = todayTx
            .flatMap { it.items }
            .groupBy { it.name }
            .mapValues { (_, list) -> list.sumOf { it.quantity } }
            .entries
            .sortedByDescending { it.value }
            .take(5)
            .joinToString(", ") { "${it.key} (${it.value} pcs)" }
            .ifBlank { "Belum ada transaksi hari ini" }

        val prompt = """
            Berikan ringkasan performa & saran praktis hari ini untuk "${settings.value.storeName}":
            - Jumlah transaksi hari ini: ${todayTx.size} ($cashCount tunai)
            - Total Omzet Hari Ini: ${CurrencyAndDateUtils.formatRupiah(omzet)}
            - Estimasi Laba Bersih: ${CurrencyAndDateUtils.formatRupiah(profit)}
            - Piutang Kasbon Baru: ${CurrencyAndDateUtils.formatRupiah(kasbonTotal)}
            - Produk Terlaris: $topProducts
            - Produk stok menipis: ${products.value.count { it.isLowStock }} item

            Berikan analisis singkat dalam 3-4 poin yang memotivasi dan 1 ide promo/strategi besok pagi.
        """.trimIndent()

        runAiPrompt("Ringkasan Performa Warung", prompt)
    }

    fun askAiCustom(question: String) {
        if (question.isBlank()) return
        val lowCount = products.value.count { it.isLowStock }
        val prompt = """
            Konteks warung "${settings.value.storeName}": ${products.value.size} produk terdaftar ($lowCount stok menipis), ${transactions.value.size} total transaksi tercatat, ${customers.value.count { it.remainingDebt > 0 }} pelanggan memiliki kasbon aktif.
            Pertanyaan pemilik warung: $question
        """.trimIndent()
        runAiPrompt("Tanya Asisten AI", prompt)
    }

    private fun runAiPrompt(actionLabel: String, prompt: String) {
        if (!isEffectivelyOnline.value) {
            _aiState.value = AiUiState.Error(
                "Asisten AI membutuhkan koneksi internet. Nonaktifkan Mode Offline terlebih dahulu."
            )
            return
        }
        aiJob?.cancel()
        aiJob = viewModelScope.launch {
            _aiState.value = AiUiState.Loading(actionLabel)
            var latestText = ""
            try {
                aiRepository.streamResponse(prompt).collect { partial ->
                    latestText = partial
                    _aiState.value = AiUiState.Streaming(actionLabel, partial)
                }
                _aiState.value = AiUiState.Success(actionLabel, latestText)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _aiState.value = AiUiState.Error(
                    e.localizedMessage ?: "Gagal terhubung ke Gemini AI. Periksa koneksi internet Anda."
                )
            }
        }
    }

    fun resetAiState() {
        aiJob?.cancel()
        _aiState.value = AiUiState.Idle
    }
}
