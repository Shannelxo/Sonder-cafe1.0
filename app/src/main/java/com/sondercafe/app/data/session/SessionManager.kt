package com.sondercafe.app.data.session

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Remembers who is logged in between app launches. Only the user id is stored: no email,
 * password or token, so there's nothing sensitive in the preferences file.
 */
@Singleton
class SessionManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    val userId: Flow<Long?> = dataStore.data
        .catch { error ->
            // A corrupt or unreadable file means "logged out", not a crash.
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { prefs -> prefs[USER_ID] }

    suspend fun setUserId(id: Long) {
        dataStore.edit { it[USER_ID] = id }
    }

    suspend fun clear() {
        dataStore.edit { it.remove(USER_ID) }
    }

    private companion object {
        val USER_ID = longPreferencesKey("user_id")
    }
}
