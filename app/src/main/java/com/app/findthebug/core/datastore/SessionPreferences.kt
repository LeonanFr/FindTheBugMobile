package com.app.findthebug.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore("findthebug_prefs")

@Singleton
class SessionPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        val SESSION_ID = stringPreferencesKey("session_id")
        val PLAYER_NAME = stringPreferencesKey("player_name")
        val SESSION_ACTIVE = stringPreferencesKey("session_active")
    }

    val sessionId: Flow<String?> = dataStore.data
        .map { preferences -> preferences[SESSION_ID] }

    val playerName: Flow<String?> = dataStore.data
        .map { preferences -> preferences[PLAYER_NAME] }

    val isSessionActive: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[SESSION_ACTIVE] == "true" }

    suspend fun saveSession(sessionId: String, playerName: String) {
        dataStore.edit { preferences ->
            preferences[SESSION_ID] = sessionId
            preferences[PLAYER_NAME] = playerName
            preferences[SESSION_ACTIVE] = "true"
        }
    }

    suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.remove(SESSION_ID)
            preferences.remove(PLAYER_NAME)
            preferences[SESSION_ACTIVE] = "false"
        }
    }
}