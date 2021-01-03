package com.arjun.janio.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.util.*


class StackTypeConverter {

    @TypeConverter
    fun fromStack(value: Stack<String>): String = gson.toJson(value)

    @TypeConverter
    fun toStack(value: String): Stack<String> = gson.fromJson(value, listType)

    companion object {
        private val gson: Gson = Gson()
        private val listType: Type = object : TypeToken<Stack<String>>() {}.type
    }
}