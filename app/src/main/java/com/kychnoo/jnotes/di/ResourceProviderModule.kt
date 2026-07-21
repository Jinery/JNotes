package com.kychnoo.jnotes.di

import com.kychnoo.jnotes.provider.AndroidResourceProvider
import com.kychnoo.jnotes.provider.ResourceProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ResourceProviderModule {
    @Binds
    @Singleton
    abstract fun bindResourceProvider(
        implementation: AndroidResourceProvider
    ): ResourceProvider
}