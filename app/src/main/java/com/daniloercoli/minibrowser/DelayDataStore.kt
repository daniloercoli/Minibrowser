package com.daniloercoli.minibrowser

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Singleton per l'accesso a DataStore
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class DelayDataStore(private val context: Context) {

    // Chiave per il valore di delay JavaScript
    private val JS_DELAY_KEY = longPreferencesKey("js_delay_ms")

    // Valore predefinito in millisecondi
    private val DEFAULT_DELAY = 200L

    // Salva il valore di delay in DataStore
    suspend fun saveJsDelay(delayMs: Long) {
        context.dataStore.edit { preferences ->
            preferences[JS_DELAY_KEY] = delayMs
        }
    }

    // Ottieni il valore corrente di delay da DataStore
    fun getJsDelay(): Flow<Long> {
        return context.dataStore.data.map { preferences ->
            preferences[JS_DELAY_KEY] ?: DEFAULT_DELAY
        }
    }
}