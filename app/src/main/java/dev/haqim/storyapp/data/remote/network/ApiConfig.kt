package dev.haqim.storyapp.data.remote.network

import dev.haqim.storyapp.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiConfig {
    private fun createRetrofit(httpClient: OkHttpClient.Builder): Retrofit {
        val loggingInterceptor =
            HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)

        val client = httpClient
            .connectTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .build()
        return Retrofit.Builder().baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    fun <ServiceClass> createService(
        serviceClass: Class<ServiceClass>
    ): ServiceClass {
        val httpClient = OkHttpClient.Builder()
        httpClient.connectTimeout(120, TimeUnit.SECONDS)
        httpClient.readTimeout(120, TimeUnit.SECONDS)
        httpClient.writeTimeout(120, TimeUnit.SECONDS)
        val retrofit = createRetrofit(httpClient)
        return retrofit.create(serviceClass)
    }

}