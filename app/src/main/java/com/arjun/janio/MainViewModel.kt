package com.arjun.janio

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import java.util.*

class MainViewModel : ViewModel() {

    private val _text by lazy { MutableLiveData<String>() }
    private val _hasFocus by lazy { MutableLiveData<Boolean>(false) }
    private val stack by lazy { Stack<String>() }

    val wordCount
        get() =
            _text.map { it.split(" ").filter { it.isNotEmpty() }.size }

    val hasFocus: LiveData<Boolean> = _hasFocus

    val isTextEmptyOrNull
        get() = _text.value.isNullOrEmpty()

    fun setText(text: String) {
        _text.value = text
    }

    fun canUndo(): Boolean = stack.isNotEmpty()

    fun undo(): String = stack.pop()

    fun setFocus(focus: Boolean) {
        _hasFocus.value = focus
    }

    fun push(text: String) {
        stack.push(text)
    }
}