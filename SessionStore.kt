package com.medspace.app.data

import android.content.Context

class SessionStore(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("medspace_session", Context.MODE_PRIVATE)

    var accessToken: String
        get() = prefs.getString("access", "") ?: ""
        set(v) { prefs.edit().putString("access", v).apply() }

    var refreshToken: String
        get() = prefs.getString("refresh", "") ?: ""
        set(v) { prefs.edit().putString("refresh", v).apply() }

    var userId: String
        get() = prefs.getString("uid", "") ?: ""
        set(v) { prefs.edit().putString("uid", v).apply() }

    val hasSession: Boolean get() = refreshToken.isNotEmpty() && userId.isNotEmpty()

    fun clear() { prefs.edit().clear().apply() }
}
