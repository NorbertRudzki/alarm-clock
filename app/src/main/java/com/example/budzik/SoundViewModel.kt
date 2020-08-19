package com.example.budzik

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SoundViewModel(application: Application): AndroidViewModel(application) {
    private val selectedSound = MutableLiveData<String>()

    fun setString(s: String){
        selectedSound.value = s
        Log.d("VM","set $s")
    }

    fun getString(): LiveData<String>{
        Log.d("VM","get ${selectedSound}")
        return  selectedSound
    }

}