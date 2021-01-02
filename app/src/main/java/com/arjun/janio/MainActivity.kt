package com.arjun.janio

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.viewbinding.ViewBinding
import com.arjun.janio.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val viewModel by viewModels<MainViewModel>()
    private val binding by viewBinding(ActivityMainBinding::inflate)
    private lateinit var undoManager: UndoRedoHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        undoManager = UndoRedoHelper(binding.textField)

        binding.textField.apply {
            setOnFocusChangeListener { _, hasFocus ->
                Log.d(TAG, "$hasFocus")
                viewModel.setFocus(hasFocus)
                binding.undo.isEnabled = !hasFocus && !viewModel.isTextEmptyOrNull
            }

            doOnTextChanged { text, start, before, count ->
//                Log.d(TAG, "$text")
//                Log.d(TAG, "$start")
//                Log.d(TAG, "$before")
//                Log.d(TAG, "$count")

                viewModel.setText(text = text.toString())

            }
        }

        binding.undo.setOnClickListener {
            if (undoManager.canUndo)
                undoManager.undo()
        }

        viewModel.wordCount.observe(this) { count ->
            viewModel.hasFocus.observe(this) {
                if (!it) binding.textView.text = "$count words."
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }

}

inline fun <T : ViewBinding> AppCompatActivity.viewBinding(
        crossinline bindingInflater: (LayoutInflater) -> T) =
        lazy(LazyThreadSafetyMode.NONE) {
            bindingInflater.invoke(layoutInflater)
        }
