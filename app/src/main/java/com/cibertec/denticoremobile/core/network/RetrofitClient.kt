package com.cibertec.denticoremobile.core.network

import android.content.Context
import com.cibertec.denticoremobile.core.security.TokenManager
import com.cibertec.denticoremobile.data.remote.DentiCoreApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Singleton que construye y expone la instancia de Retrofit para consumir la API.
 * Configura OkHttpClient con el JwtInterceptor para autenticación y un
 * HttpLoggingInterceptor para facilitar la depuración en desarrollo.
 */
object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8080/api/v1/"

    @Volatile
    private var api: DentiCoreApi? = null

    /**
     * Retorna la instancia única de [DentiCoreApi].
     * Si aún no se ha inicializado, la construye usando el contexto de aplicación.
     */
    fun getApi(context: Context): DentiCoreApi {
        return api ?: synchronized(this) {
            api ?: createApi(context.applicationContext).also { api = it }
        }
    }

    /**
     * Fuerza la reconstrucción del cliente. Útil tras un logout/login.
     */
    fun reset() {
        api = null
    }

    private fun createApi(context: Context): DentiCoreApi {
        val tokenManager = TokenManager(context)

        val jwtInterceptor = JwtInterceptor(tokenManager)

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(jwtInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(DentiCoreApi::class.java)
    }
}
