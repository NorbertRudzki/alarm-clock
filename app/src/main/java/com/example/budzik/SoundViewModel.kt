package com.example.budzik

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/** Klasa odpowiedzialna za przechowywanie wybranego dźwięku budzika w postaci LiveData*/
class SoundViewModel(application: Application) : AndroidViewModel(application) {
    private val selectedSound = MutableLiveData<String>()

    /** Ustawia nazwę dźwięku budzika */
    fun setString(s: String) {
        selectedSound.value = s
    }

    /** Zwraca nazwę dźwięku budzika */
    fun getString(): LiveData<String> {
        return selectedSound
    }
}