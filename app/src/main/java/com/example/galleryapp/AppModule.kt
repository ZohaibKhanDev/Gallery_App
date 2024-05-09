package com.example.galleryapp

import androidx.room.Room
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {

    single {
        Room.databaseBuilder(
            androidContext(),
            DataBase::class.java,
            "demo.db"

        ).allowMainThreadQueries()
            .build()
    }
    single { Repository(get()) }
    viewModelOf(::MainViewModel)
}