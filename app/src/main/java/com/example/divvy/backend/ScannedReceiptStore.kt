package com.example.divvy.backend

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.divvy.models.ParsedReceipt
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

private val Context.receiptDataStore by preferencesDataStore(name = "scanned_receipt")

@Singleton
class ScannedReceiptStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val receiptKey = stringPreferencesKey("receipt_json")

    private var _receipt: ParsedReceipt? = null

    fun store(receipt: ParsedReceipt) {
        _receipt = receipt
        persistToDataStore(receipt)
    }

    fun peek(): ParsedReceipt? {
        if (_receipt != null) return _receipt
        // Recover from DataStore if in-memory is null (e.g., after process death)
        _receipt = readFromDataStore()
        return _receipt
    }

    fun consume(): ParsedReceipt? {
        val receipt = peek()
        _receipt = null
        clearDataStore()
        return receipt
    }

    fun clear() {
        _receipt = null
        clearDataStore()
    }

    private fun persistToDataStore(receipt: ParsedReceipt) {
        try {
            runBlocking {
                context.receiptDataStore.edit { prefs ->
                    prefs[receiptKey] = json.encodeToString(receipt)
                }
            }
        } catch (e: Exception) {
            Timber.w(e, "Failed to persist receipt to DataStore")
        }
    }

    private fun readFromDataStore(): ParsedReceipt? {
        return try {
            runBlocking {
                context.receiptDataStore.data.map { prefs ->
                    prefs[receiptKey]?.let { jsonStr ->
                        json.decodeFromString<ParsedReceipt>(jsonStr)
                    }
                }.first()
            }
        } catch (e: Exception) {
            Timber.w(e, "Failed to read receipt from DataStore")
            null
        }
    }

    private fun clearDataStore() {
        try {
            runBlocking {
                context.receiptDataStore.edit { prefs ->
                    prefs.remove(receiptKey)
                }
            }
        } catch (e: Exception) {
            Timber.w(e, "Failed to clear receipt from DataStore")
        }
    }
}
