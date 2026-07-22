package com.kychnoo.jnotes.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kychnoo.jnotes.data.local.dao.NoteDao
import com.kychnoo.jnotes.data.local.entity.NoteBlockEntity
import com.kychnoo.jnotes.data.local.entity.NoteEntity

@Database(
    entities = [
        NoteEntity::class,
        NoteBlockEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}