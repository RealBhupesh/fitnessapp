package com.fitforge.app.data.database

import androidx.room.TypeConverter
import com.fitforge.app.data.model.ExerciseTemplate
import com.fitforge.app.data.model.FoodItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return gson.toJson(value ?: emptyList<String>())
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    @TypeConverter
    fun fromMapStringFloat(value: Map<String, Float>?): String {
        return gson.toJson(value ?: emptyMap<String, Float>())
    }

    @TypeConverter
    fun toMapStringFloat(value: String): Map<String, Float> {
        val mapType = object : TypeToken<Map<String, Float>>() {}.type
        return gson.fromJson(value, mapType) ?: emptyMap()
    }

    @TypeConverter
    fun fromMapStringInt(value: Map<String, Int>?): String {
        return gson.toJson(value ?: emptyMap<String, Int>())
    }

    @TypeConverter
    fun toMapStringInt(value: String): Map<String, Int> {
        val mapType = object : TypeToken<Map<String, Int>>() {}.type
        return gson.fromJson(value, mapType) ?: emptyMap()
    }

    @TypeConverter
    fun fromFoodItemList(value: List<FoodItem>?): String {
        return gson.toJson(value ?: emptyList<FoodItem>())
    }

    @TypeConverter
    fun toFoodItemList(value: String): List<FoodItem> {
        val listType = object : TypeToken<List<FoodItem>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    @TypeConverter
    fun fromExerciseTemplateList(value: List<ExerciseTemplate>?): String {
        return gson.toJson(value ?: emptyList<ExerciseTemplate>())
    }

    @TypeConverter
    fun toExerciseTemplateList(value: String): List<ExerciseTemplate> {
        val listType = object : TypeToken<List<ExerciseTemplate>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }
}
