package com.example.galleryapp

import androidx.room.Database
import androidx.room.RoomDatabase
@Database(entities = [Fav::class], version = 1)
abstract class DataBase:RoomDatabase() {
    abstract fun favDao():Dao
}