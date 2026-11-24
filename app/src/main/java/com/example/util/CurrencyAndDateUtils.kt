package com.example.util

import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object CurrencyAndDateUtils {

    private val indonesianSymbols = DecimalFormatSymbols(Locale("in", "ID")).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }

    private val rupiahFormatter = DecimalFormat("#,###", indonesianSymbols)

    fun formatRupiah(amount: Long): String {
        val absValue = kotlin.math.abs(amount)
        val formatted = rupiahFormatter.format(absValue)
        return if (amount < 0) "-Rp $formatted" else "Rp $formatted"
    }

    fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy • HH:mm", Locale("in", "ID"))
        return sdf.format(Date(timestamp))
    }

    fun formatShortTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale("in", "ID"))
        return sdf.format(Date(timestamp))
    }

    fun formatInvoiceDateCode(timestamp: Long = System.currentTimeMillis()): String {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.US)
        return sdf.format(Date(timestamp))
    }

    fun startOfTodayMillis(): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun normalizeWhatsappNumber(raw: String): String {
        val digits = raw.filter { it.isDigit() }
        return when {
            digits.startsWith("0") -> "62" + digits.drop(1)
            digits.startsWith("8") -> "62$digits"
            digits.startsWith("62") -> digits
            else -> digits
        }
    }

    fun buildWhatsAppIntent(phone: String, message: String): Intent {
        val normalized = normalizeWhatsappNumber(phone)
        val encodedText = Uri.encode(message)
        val url = if (normalized.isNotBlank()) {
            "https://wa.me/$normalized?text=$encodedText"
        } else {
            "https://wa.me/?text=$encodedText"
        }
        return Intent(Intent.ACTION_VIEW, Uri.parse(url))
    }

    fun playBeepSound() {
        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 85)
            toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 130)
        } catch (_: Exception) {
            // Ignore audio hardware unavailability
        }
    }
}
