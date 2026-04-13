package com.example.mpp.data.session

import android.content.Context

object SessionManager {
    private const val PREFS_NAME = "user_session"
    private const val KEY_TOKEN = "token"
    private const val KEY_USER_ID = "user_id"

    private var appContext: Context? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    fun restoreSession(): SessionData {
        val prefs = getPrefs()
        return SessionData(
            token = prefs?.getString(KEY_TOKEN, null),
            userId = prefs?.getString(KEY_USER_ID, null)
        )
    }

    fun saveSession(token: String?, userId: String?) {
        getPrefs()?.edit()
            ?.putString(KEY_TOKEN, token)
            ?.putString(KEY_USER_ID, userId)
            ?.apply()
    }

    fun clearSession() {
        saveSession(null, null)
    }

    fun hasSession(): Boolean {
        val session = restoreSession()
        return !session.token.isNullOrBlank() && !session.userId.isNullOrBlank()
    }

    private fun getPrefs() = appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}

data class SessionData(
    val token: String?,
    val userId: String?
)
