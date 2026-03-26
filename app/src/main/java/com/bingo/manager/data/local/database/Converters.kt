package com.bingo.manager.data.local.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Convertidores de tipo para Room.
 * Serializa/deserializa listas e objetos complejos a JSON.
 */
class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>?): String =
        gson.toJson(value ?: emptyList<String>())

    @TypeConverter
    fun toStringList(value: String): List<String> =
        gson.fromJson(value, object : TypeToken<List<String>>() {}.type) ?: emptyList()

    @TypeConverter
    fun fromLongList(value: List<Long>?): String =
        gson.toJson(value ?: emptyList<Long>())

    @TypeConverter
    fun toLongList(value: String): List<Long> =
        gson.fromJson(value, object : TypeToken<List<Long>>() {}.type) ?: emptyList()
}
