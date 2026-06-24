package com.cibertec.denticoremobile.core.network

import com.cibertec.denticoremobile.core.security.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor de OkHttp que inyecta automáticamente el header
 * Authorization: Bearer <token> en todas las peticiones protegidas.
 * El endpoint público /auth/login se excluye de esta inyección.
 */
class JwtInterceptor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url

        // No inyectar token en endpoints públicos
        if (originalUrl.encodedPath.endsWith(PUBLIC_LOGIN_PATH)) {
            return chain.proceed(originalRequest)
        }

        val token = tokenManager.getToken()
        val requestBuilder = originalRequest.newBuilder()

        if (!token.isNullOrBlank()) {
            requestBuilder.header(HEADER_AUTHORIZATION, "$BEARER_PREFIX $token")
        }

        val authenticatedRequest = requestBuilder.build()
        return chain.proceed(authenticatedRequest)
    }

    companion object {
        private const val HEADER_AUTHORIZATION = "Authorization"
        private const val BEARER_PREFIX = "Bearer"
        private const val PUBLIC_LOGIN_PATH = "/auth/login"
    }
}
