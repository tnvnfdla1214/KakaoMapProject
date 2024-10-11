package com.example.data.di

import com.example.data.service.RouteService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal class ServiceModule {

    @Provides
    @Singleton
    fun providesWordService(
        retrofit: Retrofit,
    ): RouteService = retrofit.create(RouteService::class.java)
}