package com.arjun.janio.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.arjun.janio.db.entity.EditHistory

@Database(
    entities = [EditHistory::class],
    exportSchema = false,
    version = 1
)
@TypeConverters(StackTypeConverter::class)
abstract class JanioDatabase : RoomDatabase() {

    abstract val janioDao: JanioDao

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