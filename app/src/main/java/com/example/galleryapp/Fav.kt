package com.example.galleryapp

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Fav(
    @PrimaryKey(autoGenerate = true)
    val id: Int?,
    @ColumnInfo(name = "image_url")
    val image: String,
    @ColumnInfo("video")
    val video:String
)
