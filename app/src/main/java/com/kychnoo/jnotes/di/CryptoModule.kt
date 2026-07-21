package com.kychnoo.jnotes.di

import android.content.Context
import com.kychnoo.jnotes.crypto.EncryptedFileManager
import com.kychnoo.jnotes.crypto.NoteCryptoManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CryptoModule {
    @Provides
    @Singleton
    fun provideEncryptedFileManager(@ApplicationContext context: Context): EncryptedFileManager
        = EncryptedFileManager(context)

    @Provides
    @Singleton
    fun provideNoteCryptoManager(fileManager: EncryptedFileManager): NoteCryptoManager
        = NoteCryptoManager(fileManager)
}