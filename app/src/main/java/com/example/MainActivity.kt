package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.configureAppCheck
import com.example.data.local.CustomerEntity
import com.example.data.local.ProductEntity
import com.example.data.local.StoreSettings
import com.example.data.local.TransactionEntity
import com.example.data.repository.CsvExportType
import com.example.ui.components.FloatingAiButton
import com.example.ui.components.FloatingHeaderCapsule
import com.example.ui.components.FloatingLiquidBottomNav
import com.example.ui.components.GlassToastBanner
import com.example.ui.components.LiquidGlassBackground
import com.example.ui.components.WarungTab
import com.example.ui.drawers.AddCustomerDialog
import com.example.ui.drawers.AiAssistantSheet
import com.example.ui.drawers.BarcodeScannerSheet
import com.example.ui.drawers.CartCheckoutSheet
import com.example.ui.drawers.KasbonDetailSheet
import com.example.ui.drawers.ProductFormSheet
import com.example.ui.drawers.ReceiptModalDialog
import com.example.ui.drawers.SettingsSheet
import com.example.ui.screens.KasbonScreen
import com.example.ui.screens.KasirScreen
import com.example.ui.screens.LaporanScreen
import com.example.ui.screens.StokScreen
import com.example.ui.theme.WarungKasirTheme
import com.example.ui.viewmodel.WarungViewModel
import com.example.util.CurrencyAndDateUtils
import com.example.util.PdfInvoiceGenerator
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mandatory Step 1: Configure Firebase App Check with launch intent before UI or ViewModels
        configureAppCheck(this, intent)

        enableEdgeToEdge()
        setContent {
            WarungKasirTheme {
                WarungKasirApp()
            }
        }
    }
}

@Composable
fun WarungKasirApp(
    viewModel: WarungViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val products by viewModel.products.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val kasbonEntries by viewModel.kasbonEntries.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val isOnline by viewModel.isEffectivelyOnline.collectAsStateWithLifecycle()
    val pendingSyncCount by viewModel.pendingSyncCount.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val toastEvent by viewModel.toastEvent.collectAsStateWithLifecycle()
    val cartQuantities by viewModel.cartQuantities.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val cartTotal by viewModel.cartTotal.collectAsStateWithLifecycle()
    val cartItemCount by viewModel.cartItemCount.collectAsStateWithLifecycle()
    val flashProductId by viewModel.flashProductId.collectAsStateWithLifecycle()
    val latestReceipt by viewModel.latestReceipt.collectAsStateWithLifecycle()
    val aiState by viewModel.aiState.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(WarungTab.KASIR) }
    var showCartSheet by remember { mutableStateOf(false) }
    var showScannerSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showAiSheet by remember { mutableStateOf(false) }
    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var showProductForm by remember { mutableStateOf(false) }
    var selectedCustomerForDetail by remember { mutableStateOf<CustomerEntity?>(null) }

    // Keep detail customer synced with latest Room data
    val activeDetailCustomer = selectedCustomerForDetail?.let { sel ->
        customers.find { it.id == sel.id } ?: sel
    }

    // Back handler returns to Kasir tab if on another tab
    BackHandler(enabled = selectedTab != WarungTab.KASIR) {
        selectedTab = WarungTab.KASIR
    }

    // Pending CSV export content
    var pendingCsvType by remember { mutableStateOf<CsvExportType?>(null) }
    val csvExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        val type = pendingCsvType
        pendingCsvType = null
        if (uri != null && type != null) {
            scope.launch {
                try {
                    val content = viewModel.generateCsv(type)
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        out.write(content.toByteArray(Charsets.UTF_8))
                    }
                    viewModel.showToast("Ekspor CSV ${type.name} berhasil disimpan!")
                } catch (e: Exception) {
                    viewModel.showToast("Gagal mengekspor CSV: ${e.localizedMessage}", isError = true)
                }
            }
        }
    }

    val csvImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val text = context.contentResolver.openInputStream(uri)
                        ?.bufferedReader()
                        ?.use { it.readText() }
                        .orEmpty()
                    viewModel.importProductsFromCsv(text)
                } catch (e: Exception) {
                    viewModel.showToast("Gagal membaca file CSV: ${e.localizedMessage}", isError = true)
                }
            }
        }
    }

    fun triggerExportCsv(type: CsvExportType) {
        pendingCsvType = type
        val fileName = when (type) {
            CsvExportType.TRANSAKSI -> "WarungKasir_Transaksi.csv"
            CsvExportType.STOK -> "WarungKasir_Stok_Produk.csv"
            CsvExportType.KASBON -> "WarungKasir_Buku_Kasbon.csv"
        }
        csvExportLauncher.launch(fileName)
    }

    fun downloadAndOpenTransactionPdf(tx: TransactionEntity, store: StoreSettings) {
        try {
            val file = PdfInvoiceGenerator.generateTransactionInvoicePdf(context, tx, store)
            val savedToDownloads = PdfInvoiceGenerator.saveToDownloads(context, file)
            viewModel.showToast(
                if (savedToDownloads) "Invoice PDF disimpan ke Downloads/WarungKasir"
                else "Invoice PDF dibuat: ${file.name}"
            )
            context.startActivity(PdfInvoiceGenerator.createViewOrShareIntent(context, file))
        } catch (e: Exception) {
            viewModel.showToast("Gagal membuat PDF: ${e.localizedMessage}", isError = true)
        }
    }

    fun downloadAndOpenKasbonPdf(customer: CustomerEntity, store: StoreSettings) {
        try {
            val entries = kasbonEntries.filter { it.customerId == customer.id }
            val file = PdfInvoiceGenerator.generateKasbonStatementPdf(context, customer, entries, store)
            val savedToDownloads = PdfInvoiceGenerator.saveToDownloads(context, file)
            viewModel.showToast(
                if (savedToDownloads) "Rekap Kasbon PDF disimpan ke Downloads/WarungKasir"
                else "Rekap Kasbon PDF siap dibuka"
            )
            context.startActivity(PdfInvoiceGenerator.createViewOrShareIntent(context, file))
        } catch (e: Exception) {
            viewModel.showToast("Gagal membuat PDF Kasbon: ${e.localizedMessage}", isError = true)
        }
    }

    fun launchWhatsApp(phone: String, message: String) {
        try {
            val intent = CurrencyAndDateUtils.buildWhatsAppIntent(phone, message)
            context.startActivity(intent)
        } catch (_: Exception) {
            viewModel.showToast("Aplikasi WhatsApp / browser tidak tersedia", isError = true)
        }
    }

    LiquidGlassBackground {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 840.dp)
            ) {
                FloatingHeaderCapsule(
                    storeName = settings.storeName,
                    isOnline = isOnline,
                    pendingCount = pendingSyncCount,
                    isSyncing = isSyncing,
                    onToggleOffline = { viewModel.toggleOfflineSimulation() },
                    onSyncClick = { viewModel.syncNow() },
                    onSettingsClick = { showSettingsSheet = true }
                )

                Box(modifier = Modifier.weight(1f)) {
                    Crossfade(
                        targetState = selectedTab,
                        animationSpec = tween(220),
                        label = "tab_crossfade"
                    ) { tab ->
                        when (tab) {
                            WarungTab.KASIR -> KasirScreen(
                                products = products,
                                cartQuantities = cartQuantities,
                                cartItemCount = cartItemCount,
                                cartTotal = cartTotal,
                                flashProductId = flashProductId,
                                onAddToCart = { viewModel.addToCart(it) },
                                onOpenScanner = { showScannerSheet = true },
                                onOpenCart = { showCartSheet = true }
                            )
                            WarungTab.STOK -> StokScreen(
                                products = products,
                                onAdjustStock = { id, delta -> viewModel.adjustStock(id, delta) },
                                onAddProduct = {
                                    editingProduct = null
                                    showProductForm = true
                                },
                                onEditProduct = { product ->
                                    editingProduct = product
                                    showProductForm = true
                                },
                                onDeleteProduct = { viewModel.deleteProduct(it) },
                                onExportStockCsv = { triggerExportCsv(CsvExportType.STOK) }
                            )
                            WarungTab.KASBON -> KasbonScreen(
                                customers = customers,
                                onSelectCustomer = { selectedCustomerForDetail = it },
                                onAddCustomerClick = { showAddCustomerDialog = true }
                            )
                            WarungTab.LAPORAN -> LaporanScreen(
                                transactions = transactions,
                                isSyncing = isSyncing,
                                onSyncToSheets = { viewModel.syncNow() },
                                onDownloadInvoicePdf = { tx -> downloadAndOpenTransactionPdf(tx, settings) }
                            )
                        }
                    }

                    // Floating AI Assistant Button positioned cleanly without blocking cart bar
                    val aiBottomPadding by animateDpAsState(
                        targetValue = if (selectedTab == WarungTab.KASIR && cartItemCount > 0) 182.dp else 98.dp,
                        animationSpec = tween(250),
                        label = "ai_fab_offset"
                    )
                    FloatingAiButton(
                        onClick = { showAiSheet = true },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .navigationBarsPadding()
                            .padding(end = 16.dp, bottom = aiBottomPadding)
                    )
                }
            }

            // Floating Liquid Glass Bottom Nav Bar
            FloatingLiquidBottomNav(
                selectedTab = selectedTab,
                onSelectTab = { selectedTab = it },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .widthIn(max = 600.dp)
            )

            // Glass Toast Notification Banner
            GlassToastBanner(
                toast = toastEvent,
                onDismiss = { viewModel.dismissToast() },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 74.dp)
                    .widthIn(max = 600.dp)
            )
        }
    }

    // --- Modals & Bottom Sheets ---

    if (showCartSheet) {
        CartCheckoutSheet(
            cartItems = cartItems,
            cartTotal = cartTotal,
            customers = customers,
            onUpdateQty = { id, qty -> viewModel.updateCartQuantity(id, qty) },
            onClearCart = {
                viewModel.clearCart()
                showCartSheet = false
            },
            onCheckout = { method, paid, custId, newName, newWa ->
                viewModel.completeCheckout(method, paid, custId, newName, newWa)
                showCartSheet = false
            },
            onDismiss = { showCartSheet = false }
        )
    }

    latestReceipt?.let { receiptTx ->
        ReceiptModalDialog(
            transaction = receiptTx,
            store = settings,
            onDownloadPdf = { downloadAndOpenTransactionPdf(receiptTx, settings) },
            onSendWhatsApp = {
                val statusText = if (receiptTx.paymentMethod == "TUNAI") "LUNAS (TUNAI)" else "KASBON (BELUM LUNAS)"
                val itemsText = receiptTx.items.joinToString("\n") {
                    "• ${it.name} x${it.quantity} = ${CurrencyAndDateUtils.formatRupiah(it.subtotal)}"
                }
                val msg = """
                    *${settings.storeName}*
                    Invoice: ${receiptTx.invoiceNumber}
                    Status: $statusText
                    Pelanggan: ${receiptTx.customerName}
                    
                    $itemsText
                    
                    Total: *${CurrencyAndDateUtils.formatRupiah(receiptTx.totalAmount)}*
                    ${settings.invoiceFooter}
                """.trimIndent()
                launchWhatsApp("", msg)
            },
            onDismiss = { viewModel.dismissReceiptModal() }
        )
    }

    if (showScannerSheet) {
        BarcodeScannerSheet(
            onBarcodeScanned = { code ->
                viewModel.processScannedBarcode(
                    rawBarcode = code,
                    onFound = { /* Toast & beep already triggered */ },
                    onNotFound = { /* Toast already shown */ }
                )
            },
            onSimulateRandomScan = {
                viewModel.simulateRandomBarcodeScan()
            },
            onDismiss = { showScannerSheet = false }
        )
    }

    if (showProductForm) {
        ProductFormSheet(
            existingProduct = editingProduct,
            onSave = { product ->
                viewModel.saveProduct(product)
                showProductForm = false
                editingProduct = null
            },
            onDismiss = {
                showProductForm = false
                editingProduct = null
            }
        )
    }

    if (showAddCustomerDialog) {
        AddCustomerDialog(
            onSave = { name, wa ->
                viewModel.addCustomer(name, wa)
                showAddCustomerDialog = false
            },
            onDismiss = { showAddCustomerDialog = false }
        )
    }

    activeDetailCustomer?.let { customer ->
        val customerEntries = kasbonEntries.filter { it.customerId == customer.id }
        KasbonDetailSheet(
            customer = customer,
            entries = customerEntries,
            onRecordInstallment = { amount, note ->
                viewModel.recordKasbonInstallment(customer.id, amount, note)
            },
            onDownloadPdf = { downloadAndOpenKasbonPdf(customer, settings) },
            onTagihWhatsApp = {
                val recentNotes = customerEntries
                    .filter { it.type == "UTANG" }
                    .take(3)
                    .joinToString(", ") { it.note }
                    .ifBlank { "belanja di warung" }
                val message = """
                    Halo Kak ${customer.name}, salam hangat dari *${settings.storeName}* 🙏
                    
                    Kami ingin mengingatkan rincian kasbon Kakak ($recentNotes):
                    • Total Utang: ${CurrencyAndDateUtils.formatRupiah(customer.totalDebt)}
                    • Sudah Dibayar: ${CurrencyAndDateUtils.formatRupiah(customer.paidDebt)}
                    • *Sisa Tagihan: ${CurrencyAndDateUtils.formatRupiah(customer.remainingDebt)}*
                    
                    Jika sudah ada rezeki, bisa dibayarkan langsung ke warung ya Kak. Terima kasih banyak! 😊
                """.trimIndent()
                launchWhatsApp(customer.whatsapp, message)
            },
            onDismiss = { selectedCustomerForDetail = null }
        )
    }

    if (showSettingsSheet) {
        SettingsSheet(
            settings = settings,
            onSaveSettings = { name, addr, wa, footer, url ->
                viewModel.updateStoreSettings(name, addr, wa, footer, url)
                showSettingsSheet = false
            },
            onExportCsv = { type -> triggerExportCsv(type) },
            onImportProductsCsv = {
                csvImportLauncher.launch(arrayOf("text/*", "text/csv", "application/csv", "*/*"))
            },
            onCopiedToast = { msg -> viewModel.showToast(msg) },
            onDismiss = { showSettingsSheet = false }
        )
    }

    if (showAiSheet) {
        AiAssistantSheet(
            aiState = aiState,
            customers = customers,
            onAskKulakan = { viewModel.askAiKulakan() },
            onAskBillingMessage = { cust -> viewModel.askAiBillingMessage(cust) },
            onAskDailyReport = { viewModel.askAiDailyReport() },
            onAskCustom = { q -> viewModel.askAiCustom(q) },
            onCopiedToast = { msg -> viewModel.showToast(msg) },
            onDismiss = { showAiSheet = false }
        )
    }
}
