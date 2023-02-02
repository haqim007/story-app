package dev.haqim.storyapp.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.haqim.storyapp.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class UserPreference private constructor(private val dataStore: DataStore<Preferences>) {
    
    fun getUser() = dataStore.data.map { preferences ->
        User(
            id = preferences[USER_ID_KEY] ?: "",
            name = preferences[NAME_KEY] ?: "",
            password = preferences[PASSWORD_KEY] ?: "",
            email = preferences[EMAIL_KEY] ?: "",
            hasLogin = preferences[LOGIN_STATUS] ?: false,
            token = preferences[TOKEN_KEY] ?: ""
        )
    }
    
    suspend fun saveUser(user: User) {
        dataStore.edit { preferences ->
            preferences[NAME_KEY] = user.name
            preferences[PASSWORD_KEY] = user.password
            preferences[EMAIL_KEY] = user.email
            preferences[LOGIN_STATUS] = user.hasLogin
            preferences[TOKEN_KEY] = user.token
        }
    }

    suspend fun setLanguage(code: String) {
        dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = code
        }
    }

    suspend fun login() {
        dataStore.edit { preferences ->
            preferences[LOGIN_STATUS] = true
        }
    }
    
    suspend fun logout() {
        dataStore.edit { preferences ->
            preferences[LOGIN_STATUS] = false
            preferences[USER_ID_KEY] = ""
            preferences[NAME_KEY] = ""
            preferences[PASSWORD_KEY] = ""
            preferences[EMAIL_KEY] = ""
            preferences[LOGIN_STATUS] = false
            preferences[TOKEN_KEY] = ""
            preferences[LANGUAGE_KEY] = "en"
        }
    }
    
    
    companion object {
        @Volatile
        private var INSTANCE: UserPreference? = null
        fun getInstance(dataStore: DataStore<Preferences>): UserPreference {
            return INSTANCE ?: synchronized(this) {
                val instance = UserPreference(dataStore)
                INSTANCE = instance
                instance
            }
        }

        private val USER_ID_KEY = stringPreferencesKey("userId")
        private val NAME_KEY = stringPreferencesKey("name")
        private val EMAIL_KEY = stringPreferencesKey("email")
        private val PASSWORD_KEY = stringPreferencesKey("password")
        private val LOGIN_STATUS = booleanPreferencesKey("login_status")
        private val TOKEN_KEY = stringPreferencesKey("token")
        private val LANGUAGE_KEY = stringPreferencesKey("language")
    }
}