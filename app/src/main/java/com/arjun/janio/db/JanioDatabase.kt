package com.arjun.janio.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.arjun.janio.db.entity.EditHistory

@Database(
    entities = [EditHistory::class],
    exportSchema = false,
    version = 1
)
abstract class JanioDatabase : RoomDatabase() {

    companion object {
        private const val NAME = "janio_db"
        private var instance: JanioDatabase? = null

        fun getInstance(context: Context): JanioDatabase = instance ?: synchronized(this) {
            instance ?: synchronized(this) {
                Room.databaseBuilder(context.applicationContext, JanioDatabase::class.java, NAME)
                    .fallbackToDestructiveMigration()
                    .build().also { instance = it }
            }
        }
    }
}