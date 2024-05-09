package com.example.galleryapp

class Repository(private val dataBase: DataBase) {
    fun getAllFav():List<Fav>{
        return dataBase.favDao().getAllFav()
    }

    fun Insert(fav: Fav){
        dataBase.favDao().Insert(fav)
    }
}