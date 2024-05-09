package com.example.galleryapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(private val repository: Repository):ViewModel() {

    private val _allFav= MutableStateFlow<ResultState<List<Fav>>>(ResultState.Loading)
    val allFav: StateFlow<ResultState<List<Fav>>> = _allFav.asStateFlow()


    private val _allInsert= MutableStateFlow<ResultState<Unit>>(ResultState.Loading)
    val allInsert: StateFlow<ResultState<Unit>> = _allInsert.asStateFlow()

    fun getAllFav(){
        viewModelScope.launch {
            _allFav.value=ResultState.Loading
            try {
                val response=repository.getAllFav()
                _allFav.value=ResultState.Success(response)
            }catch (e:Exception){
                _allFav.value=ResultState.Error(e)
            }
        }
    }

    fun Insert(fav: Fav){
        viewModelScope.launch {
            _allInsert.value=ResultState.Loading
            try {
                val response=repository.Insert(fav)
                _allInsert.value=ResultState.Success(response)
            }catch (e:Exception){
                _allInsert.value=ResultState.Error(e)
            }
        }
    }
}