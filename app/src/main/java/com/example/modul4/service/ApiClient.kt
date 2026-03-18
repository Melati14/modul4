package com.example.modul4.service

import com.example.modul4.model.CatFactResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

interface ApiService {
    @GET("fact")
    suspend fun getRandomFact(): Response<CatFactResponse>

}

    object ApiClient {
        private const val BASE_URL = "https://catfact.ninja"

        private val retrofit: Retrofit by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        val apiService: ApiService by lazy {
            retrofit.create(ApiService::class.java)
        }

        }