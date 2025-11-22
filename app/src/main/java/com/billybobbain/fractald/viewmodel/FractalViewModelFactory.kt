package com.billybobbain.fractald.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class FractalViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FractalViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FractalViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
