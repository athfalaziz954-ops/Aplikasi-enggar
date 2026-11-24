package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.local.CartItemPayload
import com.example.data.local.CustomerEntity
import com.example.data.local.KasbonEntryEntity
import com.example.data.local.ProductEntity
import com.example.data.local.StoreSettings
import com.example.data.local.TransactionEntity
import com.example.data.local.WarungDatabase
import com.example.util.CurrencyAndDateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

enum class CsvExportType {
    TRANSAKSI, STOK, KASBON
}

data class SyncResult(
    val success: Boolean,
    val isSimulated: Boolean,
    val syncedCount: Int,
    val message: String
)

class WarungRepository(
    private val db: WarungDatabase
) {
    private val productDao = db.productDao()
    private val transactionDao = db.transactionDao()
    private val customerDao = db.customerDao()
    private val kasbonDao = db.kasbonEntryDao()

    private val httpClient = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    val productsFlow: Flow<List<ProductEntity>> = productDao.observeAll()
    val transactionsFlow: Flow<List<TransactionEntity>> = transactionDao.observeAll()
    val customersFlow: Flow<List<CustomerEntity>> = customerDao.observeAll()
    val kasbonEntriesFlow: Flow<List<KasbonEntryEntity>> = kasbonDao.observeAll()

    suspend fun ensureInitialProductsSeeded() {
        if (productDao.count() == 0) {
            productDao.insertAll(WarungDatabase.INITIAL_SEED_PRODUCTS)
        }
    }

    suspend fun findProductByBarcode(barcode: String): ProductEntity? {
        return productDao.findByBarcode(barcode.trim())
    }

    suspend fun saveProduct(product: ProductEntity): Long {
        val updated = product.copy(updatedAt = System.currentTimeMillis(), isSynced = false)
        return productDao.insert(updated)
    }

    suspend fun deleteProduct(product: ProductEntity) {
        productDao.delete(product)
    }

    suspend fun adjustProductStock(productId: Long, delta: Int) {
        productDao.adjustStock(productId, delta)
    }

    suspend fun addCustomer(name: String, whatsapp: String): Long {
        val customer = CustomerEntity(
            name = name.trim(),
            whatsapp = whatsapp.trim(),
            lastUpdated = System.currentTimeMillis(),
            isSynced = false
        )
        return customerDao.insert(customer)
    }

    suspend fun checkoutTransaction(
        cartItems: List<CartItemPayload>,
        paymentMethod: String, // "TUNAI" or "KASBON"
        paidAmount: Long,
        customerId: Long?,
        customerNameInput: String?,
        customerWhatsappInput: String?
    ): TransactionEntity {
        val now = System.currentTimeMillis()
        val totalAmount = cartItems.sumOf { it.subtotal }

        return db.withTransaction {
            val todayCount = transactionDao.countToday(CurrencyAndDateUtils.startOfTodayMillis())
            val seq = String.format("%03d", todayCount + 1)
            val invoiceNumber = "INV/${CurrencyAndDateUtils.formatInvoiceDateCode(now)}/$seq"

            var resolvedCustomerId = customerId
            var resolvedCustomerName = "Pelanggan Umum"

            if (paymentMethod == "KASBON") {
                if (resolvedCustomerId == null && !customerNameInput.isNullOrBlank()) {
                    resolvedCustomerId = customerDao.insert(
                        CustomerEntity(
                            name = customerNameInput.trim(),
                            whatsapp = customerWhatsappInput?.trim().orEmpty(),
                            lastUpdated = now,
                            isSynced = false
                        )
                    )
                }
                val customer = resolvedCustomerId?.let { customerDao.findById(it) }
                resolvedCustomerName = customer?.name ?: (customerNameInput?.trim().orEmpty().ifBlank { "Pelanggan Kasbon" })

                if (customer != null) {
                    val effectivePaid = paidAmount.coerceIn(0L, totalAmount)
                    customerDao.update(
                        customer.copy(
                            totalDebt = customer.totalDebt + totalAmount,
                            paidDebt = customer.paidDebt + effectivePaid,
                            lastUpdated = now,
                            isSynced = false
                        )
                    )
                    val summary = cartItems.joinToString(", ") { "${it.name} x${it.quantity}" }
                    kasbonDao.insert(
                        KasbonEntryEntity(
                            customerId = customer.id,
                            type = "UTANG",
                            amount = totalAmount,
                            note = summary,
                            invoiceNumber = invoiceNumber,
                            timestamp = now,
                            isSynced = false
                        )
                    )
                    if (effectivePaid > 0L) {
                        kasbonDao.insert(
                            KasbonEntryEntity(
                                customerId = customer.id,
                                type = "CICILAN",
                                amount = effectivePaid,
                                note = "DP $invoiceNumber",
                                invoiceNumber = invoiceNumber,
                                timestamp = now + 1,
                                isSynced = false
                            )
                        )
                    }
                }
            }

            // Deduct stock
            cartItems.forEach { item ->
                productDao.adjustStock(item.productId, -item.quantity, now)
            }

            val changeOrRemaining = if (paymentMethod == "TUNAI") {
                (paidAmount - totalAmount).coerceAtLeast(0L)
            } else {
                (totalAmount - paidAmount.coerceIn(0L, totalAmount)).coerceAtLeast(0L)
            }

            val tx = TransactionEntity(
                invoiceNumber = invoiceNumber,
                timestamp = now,
                customerId = resolvedCustomerId,
                customerName = resolvedCustomerName,
                paymentMethod = paymentMethod,
                totalAmount = totalAmount,
                paidAmount = paidAmount,
                changeOrRemaining = changeOrRemaining,
                itemsJson = CartItemPayload.listToJsonString(cartItems),
                isSynced = false
            )
            val txId = transactionDao.insert(tx)
            tx.copy(id = txId)
        }
    }

    suspend fun recordKasbonInstallment(
        customerId: Long,
        amount: Long,
        note: String
    ) {
        if (amount <= 0L) return
        val now = System.currentTimeMillis()
        db.withTransaction {
            val customer = customerDao.findById(customerId) ?: return@withTransaction
            val applied = amount.coerceAtMost(customer.remainingDebt)
            if (applied <= 0L) return@withTransaction

            customerDao.update(
                customer.copy(
                    paidDebt = customer.paidDebt + applied,
                    lastUpdated = now,
                    isSynced = false
                )
            )
            kasbonDao.insert(
                KasbonEntryEntity(
                    customerId = customerId,
                    type = "CICILAN",
                    amount = applied,
                    note = note.ifBlank { "Cicilan Kasbon" },
                    timestamp = now,
                    isSynced = false
                )
            )
        }
    }

    suspend fun syncToGoogleSheets(settings: StoreSettings): SyncResult = withContext(Dispatchers.IO) {
        val unsyncedTx = transactionDao.getUnsynced()
        val allProducts = productDao.getAllOnce()
        val unsyncedProductsCount = allProducts.count { !it.isSynced }
        val unsyncedKasbon = kasbonDao.getUnsynced()
        val customersById = customerDao.getUnsynced().associateBy { it.id }

        val totalPending = unsyncedTx.size + unsyncedProductsCount + unsyncedKasbon.size
        val url = settings.webhookUrl.trim()

        if (url.isBlank()) {
            // Local simulation when webhook URL is not configured yet
            db.withTransaction {
                transactionDao.markAllSynced()
                productDao.markAllSynced()
                customerDao.markAllSynced()
                kasbonDao.markAllSynced()
            }
            return@withContext SyncResult(
                success = true,
                isSimulated = true,
                syncedCount = totalPending,
                message = if (totalPending > 0) {
                    "Simulasi sinkronisasi selesai ($totalPending data). Isi URL Apps Script di Pengaturan untuk kirim ke Google Sheets."
                } else {
                    "Semua data sudah tersinkronisasi secara lokal."
                }
            )
        }

        val payload = JSONObject().apply {
            put("storeProfile", JSONObject().apply {
                put("storeName", settings.storeName)
                put("storeAddress", settings.storeAddress)
                put("storeWhatsapp", settings.storeWhatsapp)
            })
            put("transactions", JSONArray().apply {
                unsyncedTx.forEach { tx ->
                    put(JSONObject().apply {
                        put("invoiceNumber", tx.invoiceNumber)
                        put("dateFormatted", CurrencyAndDateUtils.formatDateTime(tx.timestamp))
                        put("customerName", tx.customerName)
                        put("paymentMethod", tx.paymentMethod)
                        put("totalAmount", tx.totalAmount)
                        put("paidAmount", tx.paidAmount)
                        put("changeOrRemaining", tx.changeOrRemaining)
                        put("itemsSummary", tx.items.joinToString("; ") { "${it.name} x${it.quantity}" })
                    })
                }
            })
            put("products", JSONArray().apply {
                allProducts.forEach { p ->
                    put(JSONObject().apply {
                        put("barcode", p.barcode)
                        put("name", p.name)
                        put("category", p.category)
                        put("costPrice", p.costPrice)
                        put("sellPrice", p.sellPrice)
                        put("stock", p.stock)
                        put("minStock", p.minStock)
                    })
                }
            })
            put("kasbon", JSONArray().apply {
                unsyncedKasbon.forEach { entry ->
                    val cust = customersById[entry.customerId] ?: customerDao.findById(entry.customerId)
                    put(JSONObject().apply {
                        put("dateFormatted", CurrencyAndDateUtils.formatDateTime(entry.timestamp))
                        put("customerName", cust?.name ?: "Pelanggan")
                        put("whatsapp", cust?.whatsapp ?: "")
                        put("type", entry.type)
                        put("amount", entry.amount)
                        put("note", entry.note)
                        put("remainingDebt", cust?.remainingDebt ?: 0L)
                    })
                }
            })
        }

        try {
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = payload.toString().toRequestBody(mediaType)
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    db.withTransaction {
                        transactionDao.markAllSynced()
                        productDao.markAllSynced()
                        customerDao.markAllSynced()
                        kasbonDao.markAllSynced()
                    }
                    SyncResult(
                        success = true,
                        isSimulated = false,
                        syncedCount = totalPending,
                        message = "Berhasil sinkronisasi $totalPending data ke Google Sheets!"
                    )
                } else {
                    SyncResult(
                        success = false,
                        isSimulated = false,
                        syncedCount = 0,
                        message = "Gagal sinkronisasi ke Google Sheets (HTTP ${response.code}). Data tetap aman secara lokal."
                    )
                }
            }
        } catch (e: Exception) {
            SyncResult(
                success = false,
                isSimulated = false,
                syncedCount = 0,
                message = "Koneksi ke Google Sheets gagal: ${e.localizedMessage ?: "Periksa URL & internet"}"
            )
        }
    }

    suspend fun buildCsvContent(type: CsvExportType): String = withContext(Dispatchers.IO) {
        when (type) {
            CsvExportType.STOK -> {
                val sb = StringBuilder("Barcode,Nama_Produk,Kategori,Harga_Modal,Harga_Jual,Stok,Batas_Min\n")
                productDao.getAllOnce().forEach { p ->
                    sb.append(
                        "${escapeCsv(p.barcode)},${escapeCsv(p.name)},${escapeCsv(p.category)},${p.costPrice},${p.sellPrice},${p.stock},${p.minStock}\n"
                    )
                }
                sb.toString()
            }
            CsvExportType.TRANSAKSI -> {
                val sb = StringBuilder("No_Invoice,Tanggal,Pelanggan,Metode,Total_Tagihan,Dibayar,Kembalian_Sisa,Rincian,Status_Sync\n")
                transactionDao.getAllOnce().forEach { tx ->
                    val summary = tx.items.joinToString("; ") { "${it.name} x${it.quantity}" }
                    val status = if (tx.isSynced) "Tersinkronisasi" else "Tertunda"
                    sb.append(
                        "${escapeCsv(tx.invoiceNumber)},${escapeCsv(CurrencyAndDateUtils.formatDateTime(tx.timestamp))},${escapeCsv(tx.customerName)},${tx.paymentMethod},${tx.totalAmount},${tx.paidAmount},${tx.changeOrRemaining},${escapeCsv(summary)},$status\n"
                    )
                }
                sb.toString()
            }
            CsvExportType.KASBON -> {
                val sb = StringBuilder("Pelanggan,WhatsApp,Total_Utang,Sudah_Dibayar,Sisa_Utang\n")
                customerDao.getAllOnce().forEach { c ->
                    sb.append(
                        "${escapeCsv(c.name)},${escapeCsv(c.whatsapp)},${c.totalDebt},${c.paidDebt},${c.remainingDebt}\n"
                    )
                }
                sb.toString()
            }
        }
    }

    suspend fun importProductsFromCsv(csvText: String): Int = withContext(Dispatchers.IO) {
        val lines = csvText.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return@withContext 0
        var imported = 0
        val startIndex = if (lines.first().contains("Nama", ignoreCase = true) || lines.first().contains("Barcode", ignoreCase = true)) 1 else 0

        for (i in startIndex until lines.size) {
            val cols = parseCsvLine(lines[i])
            if (cols.size >= 6) {
                val barcode = cols[0].trim().ifBlank { "899${System.currentTimeMillis() % 1000000000}" }
                val name = cols[1].trim()
                if (name.isBlank()) continue
                val category = cols[2].trim().ifBlank { "Umum" }
                val costPrice = cols[3].trim().toLongOrNull() ?: 0L
                val sellPrice = cols[4].trim().toLongOrNull() ?: costPrice
                val stock = cols[5].trim().toIntOrNull() ?: 0
                val minStock = cols.getOrNull(6)?.trim()?.toIntOrNull() ?: 5

                val existing = productDao.findByBarcode(barcode)
                val entity = ProductEntity(
                    id = existing?.id ?: 0L,
                    name = name,
                    barcode = barcode,
                    category = category,
                    costPrice = costPrice,
                    sellPrice = sellPrice,
                    stock = stock,
                    minStock = minStock,
                    updatedAt = System.currentTimeMillis(),
                    isSynced = false
                )
                productDao.insert(entity)
                imported++
            }
        }
        imported
    }

    private fun escapeCsv(value: String): String {
        val clean = value.replace("\"", "\"\"")
        return "\"$clean\""
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        for (ch in line) {
            when {
                ch == '"' -> inQuotes = !inQuotes
                ch == ',' && !inQuotes -> {
                    result.add(current.toString())
                    current.clear()
                }
                else -> current.append(ch)
            }
        }
        result.add(current.toString())
        return result
    }
}
