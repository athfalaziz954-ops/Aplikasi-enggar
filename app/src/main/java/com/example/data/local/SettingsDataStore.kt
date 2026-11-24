package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.warungDataStore: DataStore<Preferences> by preferencesDataStore(name = "warung_settings")

data class StoreSettings(
    val storeName: String = "Warung Berkah Jaya",
    val storeAddress: String = "Jl. Melati No. 18, Pasar Baru",
    val storeWhatsapp: String = "081234567890",
    val invoiceFooter: String = "Terima kasih sudah berbelanja! Sehat & lancar selalu rezekinya.",
    val webhookUrl: String = "",
    val simulateOffline: Boolean = false,
    val lastSyncTime: Long = 0L
)

class SettingsDataStore(private val context: Context) {

    private object Keys {
        val STORE_NAME = stringPreferencesKey("store_name")
        val STORE_ADDRESS = stringPreferencesKey("store_address")
        val STORE_WHATSAPP = stringPreferencesKey("store_whatsapp")
        val INVOICE_FOOTER = stringPreferencesKey("invoice_footer")
        val WEBHOOK_URL = stringPreferencesKey("webhook_url")
        val SIMULATE_OFFLINE = booleanPreferencesKey("simulate_offline")
        val LAST_SYNC_TIME = longPreferencesKey("last_sync_time")
    }

    val settingsFlow: Flow<StoreSettings> = context.warungDataStore.data.map { prefs ->
        StoreSettings(
            storeName = prefs[Keys.STORE_NAME] ?: "Warung Berkah Jaya",
            storeAddress = prefs[Keys.STORE_ADDRESS] ?: "Jl. Melati No. 18, Pasar Baru",
            storeWhatsapp = prefs[Keys.STORE_WHATSAPP] ?: "081234567890",
            invoiceFooter = prefs[Keys.INVOICE_FOOTER]
                ?: "Terima kasih sudah berbelanja! Sehat & lancar selalu rezekinya.",
            webhookUrl = prefs[Keys.WEBHOOK_URL] ?: "",
            simulateOffline = prefs[Keys.SIMULATE_OFFLINE] ?: false,
            lastSyncTime = prefs[Keys.LAST_SYNC_TIME] ?: 0L
        )
    }

    suspend fun updateStoreProfile(
        storeName: String,
        storeAddress: String,
        storeWhatsapp: String,
        invoiceFooter: String,
        webhookUrl: String
    ) {
        context.warungDataStore.edit { prefs ->
            prefs[Keys.STORE_NAME] = storeName.ifBlank { "Warung Berkah Jaya" }
            prefs[Keys.STORE_ADDRESS] = storeAddress
            prefs[Keys.STORE_WHATSAPP] = storeWhatsapp
            prefs[Keys.INVOICE_FOOTER] = invoiceFooter
            prefs[Keys.WEBHOOK_URL] = webhookUrl.trim()
        }
    }

    suspend fun setSimulateOffline(offline: Boolean) {
        context.warungDataStore.edit { prefs ->
            prefs[Keys.SIMULATE_OFFLINE] = offline
        }
    }

    suspend fun updateLastSyncTime(timestamp: Long = System.currentTimeMillis()) {
        context.warungDataStore.edit { prefs ->
            prefs[Keys.LAST_SYNC_TIME] = timestamp
        }
    }
}
