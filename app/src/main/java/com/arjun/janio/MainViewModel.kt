package com.arjun.janio

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.arjun.janio.db.JanioDao
import com.arjun.janio.db.entity.EditHistory
import kotlinx.coroutines.launch
import java.util.*

class MainViewModel @ViewModelInject constructor(private val database: JanioDao) : ViewModel() {

    private val _text by lazy { MutableLiveData<String>() }
    private val _initialText by lazy { MutableLiveData<String>() }
    private val _hasFocus by lazy { MutableLiveData<Boolean>(false) }
    private val stack by lazy { Stack<String>() }

    val initialText: LiveData<String>
        get() = _initialText

    val wordCount
        get() =
            _text.map { it.split(" ").filter { it.isNotEmpty() }.size }

    val hasFocus: LiveData<Boolean> = _hasFocus

    val isTextEmptyOrNull
        get() = _text.value.isNullOrEmpty()


    init {
        getData()
    }

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

    private fun saveEditHistory(history: Stack<String>) {
        viewModelScope.launch {
            database.insertRecord(EditHistory(id = 1, history = history))
        }
    }

    private suspend fun getHistory(): EditHistory? = database.getEditHistory()

    fun saveData() {
        saveEditHistory(stack)
    }

    private fun getData() {
        viewModelScope.launch {
            val history = getHistory()
            Log.d(TAG, "$history")
            history?.let {
                stack.apply {
                    clear()
                    addAll(it.history)
                    if (isNotEmpty()) {
                        val text = pop()
                        _text.value = text
                        _initialText.value = text
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "MainViewModel"
    }

}