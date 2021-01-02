package com.arjun.janio

import androidx.lifecycle.*

class MainViewModel : ViewModel() {

    private val _text by lazy { MutableLiveData<String>() }
    private val _hasFocus by lazy { MutableLiveData<Boolean>(false) }

    val wordCount
        get() =
            _text.map { it.split(" ").filter { it.isNotEmpty() }.size }

    val hasFocus: LiveData<Boolean> = _hasFocus

    val isTextEmptyOrNull
        get() = _text.value.isNullOrEmpty()

    fun setText(text: String) {
        _text.value = text
    }

    fun setFocus(focus: Boolean) {
        _hasFocus.value = focus
    }
}