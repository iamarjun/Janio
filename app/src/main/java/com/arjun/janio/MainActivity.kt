package com.arjun.janio

import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.viewbinding.ViewBinding
import com.arjun.janio.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), TextUndoRedo.TextChangeInfo {

    private val binding by viewBinding(ActivityMainBinding::inflate)
    private lateinit var textUndoRedo: TextUndoRedo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        textUndoRedo = TextUndoRedo(binding.textField, this)

        binding.textField.apply {
            setOnFocusChangeListener { _, hasFocus ->

                binding.undo.isEnabled = !hasFocus || binding.textField.editableText.isNotEmpty()
                binding.textView.text = "${getWordCount(binding.textField.editableText)} words."
            }

            doOnTextChanged { text, start, before, count ->
                Log.d(TAG, "$text")
                Log.d(TAG, "$start")
                Log.d(TAG, "$before")
                Log.d(TAG, "$count")
            }
        }

        binding.undo.setOnClickListener {
            textUndoRedo.exeUndo()
            binding.textView.text = "${getWordCount(binding.textField.editableText)} words."
        }

    }

    private fun getWordCount(editable: Editable): Int = editable.split(" ").filter { it.isNotEmpty() }.size

    override fun textAction() {
        binding.undo.isEnabled = textUndoRedo.canUndo()
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
