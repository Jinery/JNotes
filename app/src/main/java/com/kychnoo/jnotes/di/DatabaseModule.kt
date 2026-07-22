package com.kychnoo.jnotes.di

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.room.Room
import com.kychnoo.jnotes.data.local.AppDatabase
import com.kychnoo.jnotes.data.local.dao.NoteDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        val isDebug = context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0

        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "jnotes_data.db"
        ).apply {
            if (isDebug) {
                fallbackToDestructiveMigration(true)
            }
        }.build()
    }

    @Provides
    @Singleton
    fun provideNoteDao(database: AppDatabase): NoteDao {
        return database.noteDao()
    }
}