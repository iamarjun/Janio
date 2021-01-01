package com.arjun.janio

import android.text.Editable
import android.text.Selection
import android.text.TextWatcher
import android.widget.EditText

class TextUndoRedo(editText: EditText, info: TextChangeInfo?) : TextWatcher {
    private var offset: Record? = null
    private val editable: Editable
    private val info: TextChangeInfo?
    private var isUndoOrRedo = false
    fun exeUndo() {
        exeUndoOrRedo(true)
    }

    fun exeRedo() {
        exeUndoOrRedo(false)
    }

    fun canUndo(): Boolean {
        return offset?.prior != null
    }

    fun canRedo(): Boolean {
        return offset?.next != null
    }

    fun cleanRecord() {
        cleanPrior()
        cleanNext()
    }

    private fun noticeTextChang() {
        info?.textAction()
    }

    private fun cleanNext() {
        while (offset?.next != null) {
            val record = offset?.next
            offset?.next = record?.next
            record?.prior = null
            record?.next = null
        }
    }

    //==============================================================================================
    private fun cleanPrior() {
        while (offset?.prior != null) {
            val record = offset?.prior
            offset?.prior = record?.prior
            record?.prior = null
            record?.next = null
        }
    }

    private fun exeUndoOrRedo(Or: Boolean) {
        if (!Or) {
            offset = offset?.next
        }
        isUndoOrRedo = true
        val temp = editable.subSequence(offset?.start!!, offset?.end!!)
        editable.replace(offset?.start!!, offset?.end!!, offset?.text)
        offset?.end = offset?.start!! + offset?.text?.length!!
        Selection.setSelection(editable, offset?.end!!)
        offset?.text = temp
        isUndoOrRedo = false
        if (Or) {
            offset = offset?.prior
        }
        noticeTextChang()
    }

    @Deprecated("")
    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        if (isUndoOrRedo) {
            return
        }
        Record(start, start + after, s.subSequence(start, start + count))
        cleanNext()
        noticeTextChang()
    }

    @Deprecated("")
    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
    }

    @Deprecated("")
    override fun afterTextChanged(s: Editable) {
    }

    interface TextChangeInfo {
        fun textAction()
    }

    private inner class Record internal constructor(
        val start: Int,
        var end: Int,
        var text: CharSequence?
    ) {
        var prior: Record? = null
        var next: Record? = null

        init {
            if (offset != null) {
                offset?.next = this
                prior = offset
            }
            offset = this
        }
    }

    init {
        editText.addTextChangedListener(this)
        editable = editText.editableText
        this.info = info
        Record(0, 0, null)
    }
}