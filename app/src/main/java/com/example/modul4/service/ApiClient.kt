package com.example.modul4.service

import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "https://reqres.in/"

    private const val API_KEY = "reqres_b9678a09eee34a8685515183fefb9a1a"

    fun getApiService(context: Context): ApiService {
        val tokenManager = TokenManager(context)


        val authInterceptor = Interceptor { chain ->
            val requestBuilder = chain.request().newBuilder()

            // Menambahkan x-Api-Key untuk semua request
            requestBuilder.addHeader("x-api-key", API_KEY)

            tokenManager.getToken()?.let { token ->
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }
            chain.proceed(requestBuilder.build())
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
