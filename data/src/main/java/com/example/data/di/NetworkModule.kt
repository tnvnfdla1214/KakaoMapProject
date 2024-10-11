package com.example.data.di

import android.content.Context
import com.example.data.network.AuthInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Provides
    @Singleton
    fun providesGsonConverter(): GsonConverterFactory = GsonConverterFactory.create()

    @Provides
    @Singleton
    fun providesOkHttpClient(
        @ApplicationContext context: Context,
    ): OkHttpClient {
        // HttpLoggingInterceptor 추가 및 로그 레벨 설정
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            // 로그 레벨을 BODY로 설정하면 요청과 응답의 모든 세부사항이 출력됩니다.
            level = HttpLoggingInterceptor.Level.BODY
        }


        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context))
            .addInterceptor(loggingInterceptor)
            .followRedirects(true)
            .followSslRedirects(true)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun providesRetrofit(
        converterFactory: GsonConverterFactory,
        client: OkHttpClient,
    ): Retrofit = getRetrofit("https://taxi-openapi.sandbox.onkakao.net/api/v1/coding-assignment/", converterFactory, client)

    private fun getRetrofit(
        url: String,
        converterFactory: GsonConverterFactory,
        client: OkHttpClient,
    ) = Retrofit
        .Builder()
        .baseUrl(url)
        .addConverterFactory(converterFactory)
        .client(client)
        .build()


}