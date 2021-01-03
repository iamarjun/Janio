package com.arjun.janio.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.arjun.janio.db.entity.EditHistory
import java.util.*

@Dao
interface JanioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(editHistory: EditHistory)

    @Query("UPDATE EDITHISTORY SET HISTORY = :history ")
    suspend fun update(history: Stack<String>)

    @Query("SELECT * FROM EDITHISTORY")
    suspend fun getEditHistory(): EditHistory?
}