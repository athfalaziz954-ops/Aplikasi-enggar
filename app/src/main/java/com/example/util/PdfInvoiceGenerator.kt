package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.data.local.CustomerEntity
import com.example.data.local.KasbonEntryEntity
import com.example.data.local.StoreSettings
import com.example.data.local.TransactionEntity
import java.io.File
import java.io.FileOutputStream

object PdfInvoiceGenerator {

    private const val PAGE_WIDTH = 420
    private const val MARGIN = 24f

    fun generateTransactionInvoicePdf(
        context: Context,
        transaction: TransactionEntity,
        store: StoreSettings
    ): File {
        val items = transaction.items
        val estimatedHeight = (390 + items.size * 34).coerceAtLeast(520)
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, estimatedHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#475569")
            textSize = 9.5f
        }
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#64748B")
            textSize = 9.5f
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 10f
        }
        val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 10.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#CBD5E1")
            strokeWidth = 1f
            pathEffect = DashPathEffect(floatArrayOf(6f, 4f), 0f)
        }
        val solidLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E2E8F0")
            strokeWidth = 1f
        }

        var y = 32f
        val contentRight = PAGE_WIDTH - MARGIN

        // Header Store Info
        canvas.drawText(store.storeName, MARGIN, y, titlePaint)
        y += 15f
        if (store.storeAddress.isNotBlank()) {
            canvas.drawText(store.storeAddress.take(48), MARGIN, y, subtitlePaint)
            y += 13f
        }
        if (store.storeWhatsapp.isNotBlank()) {
            canvas.drawText("WhatsApp: ${store.storeWhatsapp}", MARGIN, y, subtitlePaint)
            y += 14f
        }

        y += 6f
        canvas.drawLine(MARGIN, y, contentRight, y, solidLinePaint)
        y += 18f

        // Invoice metadata & payment status badge
        canvas.drawText("No. Invoice", MARGIN, y, labelPaint)
        canvas.drawText(transaction.invoiceNumber, MARGIN + 80f, y, boldPaint)

        val isLunas = transaction.paymentMethod == "TUNAI"
        val badgeText = if (isLunas) "LUNAS (TUNAI)" else "KASBON (BELUM LUNAS)"
        val badgeBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isLunas) Color.parseColor("#D1FAE5") else Color.parseColor("#FEF3C7")
        }
        val badgeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isLunas) Color.parseColor("#047857") else Color.parseColor("#B45309")
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val badgeW = badgeTextPaint.measureText(badgeText) + 16f
        val badgeRect = RectF(contentRight - badgeW, y - 12f, contentRight, y + 5f)
        canvas.drawRoundRect(badgeRect, 8f, 8f, badgeBgPaint)
        canvas.drawText(badgeText, badgeRect.left + 8f, y - 1f, badgeTextPaint)

        y += 16f
        canvas.drawText("Tanggal", MARGIN, y, labelPaint)
        canvas.drawText(CurrencyAndDateUtils.formatDateTime(transaction.timestamp), MARGIN + 80f, y, bodyPaint)
        y += 15f
        canvas.drawText("Pelanggan", MARGIN, y, labelPaint)
        canvas.drawText(transaction.customerName, MARGIN + 80f, y, boldPaint)

        y += 14f
        canvas.drawLine(MARGIN, y, contentRight, y, dividerPaint)
        y += 16f

        // Table Header
        val tableHeaderBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F1F5F9")
        }
        canvas.drawRoundRect(RectF(MARGIN, y - 12f, contentRight, y + 6f), 6f, 6f, tableHeaderBg)
        canvas.drawText("Barang", MARGIN + 6f, y, boldPaint.apply { textSize = 9.5f })
        drawRightText(canvas, "Qty", MARGIN + 200f, y, boldPaint)
        drawRightText(canvas, "Harga", MARGIN + 282f, y, boldPaint)
        drawRightText(canvas, "Subtotal", contentRight - 6f, y, boldPaint)
        y += 18f

        // Items
        bodyPaint.textSize = 9.5f
        items.forEach { item ->
            val displayName = if (item.name.length > 25) item.name.take(24) + "…" else item.name
            canvas.drawText(displayName, MARGIN + 6f, y, bodyPaint)
            drawRightText(canvas, "${item.quantity}", MARGIN + 200f, y, bodyPaint)
            drawRightText(canvas, CurrencyAndDateUtils.formatRupiah(item.sellPrice), MARGIN + 282f, y, bodyPaint)
            drawRightText(canvas, CurrencyAndDateUtils.formatRupiah(item.subtotal), contentRight - 6f, y, boldPaint)
            y += 18f
        }

        y += 4f
        canvas.drawLine(MARGIN, y, contentRight, y, dividerPaint)
        y += 18f

        // Summary Totals
        boldPaint.textSize = 11f
        canvas.drawText("Total Tagihan", MARGIN, y, boldPaint)
        drawRightText(canvas, CurrencyAndDateUtils.formatRupiah(transaction.totalAmount), contentRight, y, boldPaint)
        y += 17f

        bodyPaint.textSize = 10f
        val paidLabel = if (isLunas) "Dibayar (Tunai)" else "Uang Muka (DP)"
        canvas.drawText(paidLabel, MARGIN, y, labelPaint)
        drawRightText(canvas, CurrencyAndDateUtils.formatRupiah(transaction.paidAmount), contentRight, y, bodyPaint)
        y += 17f

        val tailLabel = if (isLunas) "Kembalian" else "Sisa Kasbon"
        val tailColor = if (isLunas) Color.parseColor("#047857") else Color.parseColor("#B45309")
        val tailPaint = Paint(boldPaint).apply { color = tailColor; textSize = 11f }
        canvas.drawText(tailLabel, MARGIN, y, tailPaint)
        drawRightText(canvas, CurrencyAndDateUtils.formatRupiah(transaction.changeOrRemaining), contentRight, y, tailPaint)

        y += 26f
        canvas.drawLine(MARGIN, y, contentRight, y, solidLinePaint)
        y += 18f

        // Footer
        val footerPaint = Paint(subtitlePaint).apply { textAlign = Paint.Align.CENTER }
        val footerText = store.invoiceFooter.ifBlank { "Terima kasih sudah berbelanja!" }
        canvas.drawText(footerText.take(56), PAGE_WIDTH / 2f, y, footerPaint)
        if (footerText.length > 56) {
            y += 13f
            canvas.drawText(footerText.drop(56).take(56), PAGE_WIDTH / 2f, y, footerPaint)
        }

        document.finishPage(page)

        val dir = File(context.cacheDir, "invoices").apply { mkdirs() }
        val safeName = transaction.invoiceNumber.replace("/", "-")
        val file = File(dir, "$safeName.pdf")
        FileOutputStream(file).use { out -> document.writeTo(out) }
        document.close()
        return file
    }

    fun generateKasbonStatementPdf(
        context: Context,
        customer: CustomerEntity,
        entries: List<KasbonEntryEntity>,
        store: StoreSettings
    ): File {
        val visibleEntries = entries.take(20)
        val estimatedHeight = (380 + visibleEntries.size * 24).coerceAtLeast(520)
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, estimatedHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#64748B")
            textSize = 9.5f
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 9.5f
        }
        val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 10.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#CBD5E1")
            strokeWidth = 1f
            pathEffect = DashPathEffect(floatArrayOf(6f, 4f), 0f)
        }

        var y = 32f
        val contentRight = PAGE_WIDTH - MARGIN

        canvas.drawText(store.storeName, MARGIN, y, titlePaint)
        y += 15f
        canvas.drawText("REKAP TAGIHAN KASBON PELANGGAN", MARGIN, y, boldPaint.apply {
            color = Color.parseColor("#059669")
            textSize = 10f
        })
        y += 14f
        canvas.drawText(
            "Dicetak: ${CurrencyAndDateUtils.formatDateTime(System.currentTimeMillis())}",
            MARGIN,
            y,
            labelPaint
        )
        y += 14f
        canvas.drawLine(MARGIN, y, contentRight, y, dividerPaint)
        y += 18f

        boldPaint.color = Color.parseColor("#0F172A")
        canvas.drawText("Pelanggan", MARGIN, y, labelPaint)
        canvas.drawText(customer.name, MARGIN + 85f, y, boldPaint)
        y += 15f
        canvas.drawText("No. WhatsApp", MARGIN, y, labelPaint)
        canvas.drawText(customer.whatsapp.ifBlank { "-" }, MARGIN + 85f, y, bodyPaint)
        y += 15f

        val isSettled = customer.remainingDebt <= 0L
        val statusText = if (isSettled) "LUNAS" else "BELUM LUNAS"
        val statusColor = if (isSettled) Color.parseColor("#047857") else Color.parseColor("#B45309")
        canvas.drawText("Status", MARGIN, y, labelPaint)
        canvas.drawText(statusText, MARGIN + 85f, y, Paint(boldPaint).apply { color = statusColor })

        y += 18f
        canvas.drawLine(MARGIN, y, contentRight, y, dividerPaint)
        y += 18f

        // History header
        canvas.drawText("Tanggal", MARGIN, y, boldPaint)
        canvas.drawText("Keterangan", MARGIN + 105f, y, boldPaint)
        drawRightText(canvas, "Nominal", contentRight, y, boldPaint)
        y += 16f

        visibleEntries.forEach { entry ->
            val dateText = CurrencyAndDateUtils.formatDateTime(entry.timestamp).take(11)
            val prefix = if (entry.type == "CICILAN") "Bayar: " else "Utang: "
            val desc = (prefix + entry.note).let { if (it.length > 24) it.take(23) + "…" else it }
            val amountColor = if (entry.type == "CICILAN") Color.parseColor("#047857") else Color.parseColor("#0F172A")
            val amountText = (if (entry.type == "CICILAN") "- " else "+ ") + CurrencyAndDateUtils.formatRupiah(entry.amount)

            canvas.drawText(dateText, MARGIN, y, labelPaint)
            canvas.drawText(desc, MARGIN + 105f, y, bodyPaint)
            drawRightText(canvas, amountText, contentRight, y, Paint(bodyPaint).apply { color = amountColor })
            y += 18f
        }

        y += 8f
        canvas.drawLine(MARGIN, y, contentRight, y, dividerPaint)
        y += 18f

        canvas.drawText("Total Utang Tercatat", MARGIN, y, labelPaint)
        drawRightText(canvas, CurrencyAndDateUtils.formatRupiah(customer.totalDebt), contentRight, y, bodyPaint)
        y += 16f
        canvas.drawText("Total Sudah Dibayar", MARGIN, y, labelPaint)
        drawRightText(canvas, CurrencyAndDateUtils.formatRupiah(customer.paidDebt), contentRight, y, Paint(bodyPaint).apply {
            color = Color.parseColor("#047857")
        })
        y += 18f
        val sisaPaint = Paint(boldPaint).apply {
            color = if (isSettled) Color.parseColor("#047857") else Color.parseColor("#B45309")
            textSize = 12f
        }
        canvas.drawText("Sisa Tagihan", MARGIN, y, sisaPaint)
        drawRightText(canvas, CurrencyAndDateUtils.formatRupiah(customer.remainingDebt), contentRight, y, sisaPaint)

        y += 26f
        val footerPaint = Paint(labelPaint).apply { textAlign = Paint.Align.CENTER }
        canvas.drawText(store.invoiceFooter.take(56), PAGE_WIDTH / 2f, y, footerPaint)

        document.finishPage(page)

        val dir = File(context.cacheDir, "invoices").apply { mkdirs() }
        val safeCustomer = customer.name.replace(Regex("[^A-Za-z0-9]"), "_").take(20)
        val file = File(dir, "Kasbon_${safeCustomer}.pdf")
        FileOutputStream(file).use { out -> document.writeTo(out) }
        document.close()
        return file
    }

    fun saveToDownloads(context: Context, pdfFile: File): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, pdfFile.name)
                    put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/WarungKasir")
                }
                val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        pdfFile.inputStream().use { input -> input.copyTo(out) }
                    }
                    true
                } else false
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    fun createViewOrShareIntent(context: Context, pdfFile: File): Intent {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )
        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return if (viewIntent.resolveActivity(context.packageManager) != null) {
            Intent.createChooser(viewIntent, "Buka Invoice PDF")
        } else {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            Intent.createChooser(shareIntent, "Simpan / Bagikan Invoice PDF")
        }
    }

    private fun drawRightText(canvas: Canvas, text: String, rightX: Float, y: Float, paint: Paint) {
        val width = paint.measureText(text)
        canvas.drawText(text, rightX - width, y, paint)
    }
}
