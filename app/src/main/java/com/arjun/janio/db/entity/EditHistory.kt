package com.arjun.janio.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class EditHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val history: Stack<String>
)