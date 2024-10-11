package com.example.data.di

import com.example.data.RouteRepository
import com.example.data.service.DefaultRouteRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindRouteRepository(routeRepository: DefaultRouteRepository): RouteRepository
}