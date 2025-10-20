package com.example.finalfeliz.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userPrefsDataStore by preferencesDataStore("user_prefs")

class SessionManager(private val context: Context) {

    private val KEY_USER_ID = longPreferencesKey("current_user_id")

    val currentUserIdFlow: Flow<Long?> =
        context.userPrefsDataStore.data.map { prefs -> prefs[KEY_USER_ID] }

    suspend fun setCurrentUserId(id: Long) {
        context.userPrefsDataStore.edit { it[KEY_USER_ID] = id }
    }

    suspend fun clearCurrentUserId() {
        context.userPrefsDataStore.edit { it.remove(KEY_USER_ID) }
    }
}
