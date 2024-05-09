package com.example.galleryapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface Dao {
    @Query("SELECT * FROM Fav")
    fun getAllFav():List<Fav>
    @Insert
    fun Insert(fav: Fav)
}