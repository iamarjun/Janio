package com.arjun.janio

import android.content.SharedPreferences
import android.text.Editable
import android.text.Selection
import android.text.TextUtils
import android.text.TextWatcher
import android.text.style.UnderlineSpan
import android.util.Log
import android.widget.TextView
import java.util.*

class UndoRedoHelper(private val mTextView: TextView) {
    private var mIsUndoOrRedo = false
    private val mEditHistory: EditHistory
    private val mChangeListener: EditTextChangeListener

    // =================================================================== //
    fun disconnect() {
        mTextView.removeTextChangedListener(mChangeListener)
    }

    fun setMaxHistorySize(maxHistorySize: Int) {
        mEditHistory.setMaxHistorySize(maxHistorySize)
    }

    fun clearHistory() {
        mEditHistory.clear()
    }

    val canUndo: Boolean
        get() = mEditHistory.mmPosition > 0

    fun undo() {
        val edit: EditItem = mEditHistory.previous ?: return
        val text = mTextView.editableText
        val start = edit.mmStart
        val end = start + if (edit.mmAfter != null) edit.mmAfter!!.length else 0
        mIsUndoOrRedo = true
        text.replace(start, end, edit.mmBefore)
        mIsUndoOrRedo = false
        for (o in text.getSpans(0, text.length, UnderlineSpan::class.java)) {
            text.removeSpan(o)
        }
        Selection.setSelection(
            text,
            if (edit.mmBefore == null) start else start + edit.mmBefore!!.length
        )
    }

    val canRedo: Boolean
        get() = mEditHistory.mmPosition < mEditHistory.mmHistory.size

    fun redo() {
        val edit: EditItem = mEditHistory.next ?: return
        val text = mTextView.editableText
        val start = edit.mmStart
        val end = start + if (edit.mmBefore != null) edit.mmBefore!!.length else 0
        mIsUndoOrRedo = true
        text.replace(start, end, edit.mmAfter)
        mIsUndoOrRedo = false

        // This will get rid of underlines inserted when editor tries to come
        // up with a suggestion.
        for (o in text.getSpans(0, text.length, UnderlineSpan::class.java)) {
            text.removeSpan(o)
        }
        Selection.setSelection(
            text,
            if (edit.mmAfter == null) start else start + edit.mmAfter!!.length
        )
    }

    fun storePersistentState(editor: SharedPreferences.Editor, prefix: String) {
        // Store hash code of text in the editor so that we can check if the
        // editor contents has changed.
        editor.putString("$prefix.hash", mTextView.text.toString().hashCode().toString())
        editor.putInt("$prefix.maxSize", mEditHistory.mmMaxHistorySize)
        editor.putInt("$prefix.position", mEditHistory.mmPosition)
        editor.putInt("$prefix.size", mEditHistory.mmHistory.size)
        for ((i, ei) in mEditHistory.mmHistory.withIndex()) {
            val pre = "$prefix.$i"
            editor.putInt("$pre.start", ei.mmStart)
            editor.putString("$pre.before", ei.mmBefore.toString())
            editor.putString("$pre.after", ei.mmAfter.toString())
        }
    }

    @Throws(IllegalStateException::class)
    fun restorePersistentState(sp: SharedPreferences, prefix: String): Boolean {
        val ok = doRestorePersistentState(sp, prefix)
        if (!ok) {
            mEditHistory.clear()
        }
        return ok
    }

    private fun doRestorePersistentState(sp: SharedPreferences, prefix: String): Boolean {
        val hash = sp.getString("$prefix.hash", null)
            ?: // No state to be restored.
            return true
        if (Integer.valueOf(hash) != mTextView.text.toString().hashCode()) {
            return false
        }
        mEditHistory.clear()
        mEditHistory.mmMaxHistorySize = sp.getInt("$prefix.maxSize", -1)
        val count = sp.getInt("$prefix.size", -1)
        if (count == -1) {
            return false
        }
        for (i in 0 until count) {
            val pre = "$prefix.$i"
            val start = sp.getInt("$pre.start", -1)
            val before = sp.getString("$pre.before", null)
            val after = sp.getString("$pre.after", null)
            if (start == -1 || before == null || after == null) {
                return false
            }
            mEditHistory.add(EditItem(start, before, after))
        }
        mEditHistory.mmPosition = sp.getInt("$prefix.position", -1)
        return mEditHistory.mmPosition != -1
    }

    // =================================================================== //
    internal enum class ActionType {
        INSERT, DELETE, PASTE, NOT_DEF
    }

    private class EditHistory {
        val mmHistory = LinkedList<EditItem>()
        var mmPosition = 0
        var mmMaxHistorySize = -1
        fun clear() {
            mmPosition = 0
            mmHistory.clear()
        }

        fun add(item: EditItem) {
            while (mmHistory.size > mmPosition) {
                mmHistory.removeLast()
            }
            mmHistory.add(item)
            mmPosition++
            if (mmMaxHistorySize >= 0) {
                trimHistory()
            }
        }

        fun setMaxHistorySize(maxHistorySize: Int) {
            mmMaxHistorySize = maxHistorySize
            if (mmMaxHistorySize >= 0) {
                trimHistory()
            }
        }

        private fun trimHistory() {
            while (mmHistory.size > mmMaxHistorySize) {
                mmHistory.removeFirst()
                mmPosition--
            }
            if (mmPosition < 0) {
                mmPosition = 0
            }
        }

        val current: EditItem?
            get() = if (mmPosition == 0) {
                null
            } else mmHistory[mmPosition - 1]
        val previous: EditItem?
            get() {
                if (mmPosition == 0) {
                    return null
                }
                mmPosition--
                return mmHistory[mmPosition]
            }
        val next: EditItem?
            get() {
                if (mmPosition >= mmHistory.size) {
                    return null
                }
                val item = mmHistory[mmPosition]
                mmPosition++
                return item
            }
    }

    private class EditItem(
        var mmStart: Int,
        var mmBefore: CharSequence?,
        var mmAfter: CharSequence?
    ) {
        override fun toString(): String {
            return "EditItem{" +
                    "mmStart=" + mmStart +
                    ", mmBefore=" + mmBefore +
                    ", mmAfter=" + mmAfter +
                    '}'
        }
    }

    private inner class EditTextChangeListener : TextWatcher {
        private var mBeforeChange: CharSequence? = null
        private var mAfterChange: CharSequence? = null
        private var lastActionType = ActionType.NOT_DEF
        private var lastActionTime: Long = 0
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            if (mIsUndoOrRedo) {
                return
            }
            mBeforeChange = s.subSequence(start, start + count)
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            if (mIsUndoOrRedo) {
                return
            }
            mAfterChange = s.subSequence(start, start + count)
            makeBatch(start)
            Log.d(TAG, "$start")
        }

        private fun makeBatch(start: Int) {
            val at = actionType
            val editItem: EditItem? = mEditHistory.current
            if (lastActionType != at || ActionType.PASTE == at || System.currentTimeMillis() - lastActionTime > 1000 || editItem == null) {
                mEditHistory.add(EditItem(start, mBeforeChange, mAfterChange))
            } else {
                if (at == ActionType.DELETE) {
                    editItem.mmStart = start
                    editItem.mmBefore = TextUtils.concat(mBeforeChange, editItem.mmBefore)
                } else {
                    editItem.mmAfter = TextUtils.concat(editItem.mmAfter, mAfterChange)
                }
            }
            lastActionType = at
            lastActionTime = System.currentTimeMillis()
        }

        private val actionType: ActionType
            get() = if (!TextUtils.isEmpty(mBeforeChange) && TextUtils.isEmpty(mAfterChange)) {
                ActionType.DELETE
            } else if (TextUtils.isEmpty(mBeforeChange) && !TextUtils.isEmpty(mAfterChange)) {
                ActionType.INSERT
            } else {
                ActionType.PASTE
            }

        override fun afterTextChanged(s: Editable) {}
    }

    companion object {
        private val TAG = UndoRedoHelper::class.java.canonicalName
    }

    // =================================================================== //
    init {
        mEditHistory = EditHistory()
        mChangeListener = EditTextChangeListener()
        mTextView.addTextChangedListener(mChangeListener)
    }
}