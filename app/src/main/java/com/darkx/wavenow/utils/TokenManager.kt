package com.darkx.wavenow.utils

import android.content.Context
import android.content.SharedPreferences
import com.darkx.wavenow.models.User
import com.google.gson.Gson

/**
 * Handles saving/reading the JWT token returned by /api/auth/login or /api/auth/register.
 * This token is what connects the app to the server for every future request.
 */
class TokenManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("wavenow_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_USER = "current_user"
    }

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun saveUser(user: User) {
        prefs.edit().putString(KEY_USER, Gson().toJson(user)).apply()
    }

    fun getUser(): User? {
        val json = prefs.getString(KEY_USER, null) ?: return null
        return Gson().fromJson(json, User::class.java)
    }

    fun isLoggedIn(): Boolean = !getToken().isNullOrEmpty()

    fun clear() {
        prefs.edit().clear().apply()
    }
}
