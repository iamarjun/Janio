package com.arjun.janio

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map

class MainViewModel : ViewModel() {

    private val _text by lazy { MutableLiveData<String>() }
    private val _hasFocus by lazy { MutableLiveData<Boolean>() }
    val wordCount = _text.map { it.split(" ").filter { it.isNotEmpty() }.size }

    val text: LiveData<String> = _text
    val hasFocus: LiveData<Boolean> = _hasFocus

    fun setText(text: String) {
        _text.value = text
    }

    fun setFocus(focus :Boolean) {
        _hasFocus.value = focus
    }
}