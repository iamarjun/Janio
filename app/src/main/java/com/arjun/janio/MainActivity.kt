package com.arjun.janio

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.viewbinding.ViewBinding
import com.arjun.janio.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), TextUndoRedo.TextChangeInfo {

    private val viewModel by viewModels<MainViewModel>()
    private val binding by viewBinding(ActivityMainBinding::inflate)
    private lateinit var undoManager: UndoRedoHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        undoManager = UndoRedoHelper(binding.textField)

//        textUndoRedo = TextUndoRedo(binding.textField, this)

        binding.textField.apply {
            setOnFocusChangeListener { _, hasFocus ->
                viewModel.setFocus(hasFocus)
            }

            doOnTextChanged { text, start, before, count ->
                Log.d(TAG, "$text")
                Log.d(TAG, "$start")
                Log.d(TAG, "$before")
                Log.d(TAG, "$count")

                viewModel.setText(text = text.toString())

            }
        }

        binding.undo.setOnClickListener {
            if (undoManager.canUndo)
                undoManager.undo()
        }

        viewModel.hasFocus.observe(this) { hasFocus ->
            binding.undo.isEnabled = !hasFocus || !viewModel.text.value.isNullOrEmpty()

            viewModel.wordCount.observe(this) { count ->

                if (!hasFocus) binding.textView.text = "$count words."
            }
        }

    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: ${binding.textField.text}")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop: ${binding.textField.text}")
    }


    override fun textAction() {
//        binding.undo.isEnabled = textUndoRedo.canUndo()
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
