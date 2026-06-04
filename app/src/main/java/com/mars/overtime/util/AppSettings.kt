package com.mars.overtime.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

object AppSettings {
    private val USE_MIUIX = booleanPreferencesKey("use_miux")
    private val AUTO_SYNC = booleanPreferencesKey("auto_sync")

    suspend fun saveUseMiux(context: Context, useMiuix: Boolean) {
        context.dataStore.edit { settings ->
            settings[USE_MIUIX] = useMiuix
        }
    }

    suspend fun saveAutoSync(context: Context, autoSync: Boolean) {
        context.dataStore.edit { settings ->
            settings[AUTO_SYNC] = autoSync
        }
    }

    fun getUseMiux(context: Context): Flow<Boolean> {
        return context.dataStore.data
            .map { preferences ->
                preferences[USE_MIUIX] ?: false
            }
    }

    fun getAutoSync(context: Context): Flow<Boolean> {
        return context.dataStore.data
            .map { preferences ->
                preferences[AUTO_SYNC] ?: true
            }
    }
}
