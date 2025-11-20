package com.example.gestindeasistencia.data.remote

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory


object ApiClient {

    private const val BASE_URL = "http://10.0.2.2:8080/api/"

    fun getClient(context: Context): ApiService {
        val tokenProvider = { com.example.gestindeasistencia.utils.SecurePrefs.getToken(context) }

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenProvider))
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(ScalarsConverterFactory.create()) // <-- acepta String
            .addConverterFactory(GsonConverterFactory.create())    // <-- JSON
            .build()
            .create(ApiService::class.java)

    }
}