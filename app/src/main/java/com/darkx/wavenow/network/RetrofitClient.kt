package com.darkx.wavenow.network

import android.content.Context
import com.darkx.wavenow.utils.Constants
import com.darkx.wavenow.utils.TokenManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private var api: ApiService? = null

    /**
     * Every REST call made through this client automatically attaches:
     *   Authorization: Bearer <token>
     * using whatever token TokenManager currently has saved.
     */
    fun getApi(context: Context): ApiService {
        if (api != null) return api!!

        val tokenManager = TokenManager(context.applicationContext)

        val authInterceptor = Interceptor { chain ->
            val original = chain.request()
            val token = tokenManager.getToken()

            val requestBuilder = original.newBuilder()
            if (!token.isNullOrEmpty()) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }
            chain.proceed(requestBuilder.build())
        }

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(ApiService::class.java)
        return api!!
    }
}
