package com.cibertec.denticoremobile.core.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Gestor seguro del Token JWT utilizando EncryptedSharedPreferences.
 * El token nunca se almacena en texto plano ni en memoria estática vulnerable.
 */
class TokenManager(context: Context) {

    private val masterKey: MasterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_FILE_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    /**
     * Persiste el token JWT de forma encriptada.
     */
    fun saveToken(token: String) {
        encryptedPreferences.edit()
            .putString(KEY_TOKEN, token)
            .apply()
    }

    /**
     * Recupera el token JWT almacenado, o null si no existe.
     */
    fun getToken(): String? {
        return encryptedPreferences.getString(KEY_TOKEN, null)
    }

    /**
     * Elimina el token JWT almacenado.
     */
    fun clearToken() {
        encryptedPreferences.edit()
            .remove(KEY_TOKEN)
            .apply()
    }

    /**
     * Indica si existe un token almacenado.
     */
    fun hasToken(): Boolean {
        return !getToken().isNullOrBlank()
    }

    companion object {
        private const val PREFS_FILE_NAME = "denticore_secure_prefs"
        private const val KEY_TOKEN = "jwt_token"
    }
}
