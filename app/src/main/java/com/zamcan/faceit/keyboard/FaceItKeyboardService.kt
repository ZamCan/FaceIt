package com.zamcan.faceit.keyboard

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.inputmethodservice.InputMethodService
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Minimal, offline FaceIt IME. It never records keystrokes or requests network access.
 * Android users must explicitly enable and select it in system keyboard settings.
 */
class FaceItKeyboardService : InputMethodService() {
    private var numericMode = false
    private var shift = false
    private var root: LinearLayout? = null

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        val inputClass = attribute?.inputType?.and(InputType.TYPE_MASK_CLASS)
        numericMode = inputClass == InputType.TYPE_CLASS_NUMBER ||
            inputClass == InputType.TYPE_CLASS_PHONE
        shift = false
    }

    override fun onCreateInputView(): View {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(5), dp(7), dp(5), dp(7))
            setBackgroundColor(Color.rgb(23, 25, 31))
        }
        renderKeys()
        return root!!
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        renderKeys()
    }

    private fun renderKeys() {
        val container = root ?: return
        container.removeAllViews()
        if (numericMode) {
            listOf(listOf("1","2","3"), listOf("4","5","6"), listOf("7","8","9"), listOf("−","0","⌫"))
                .forEach { addRow(container, it) }
            addRow(container, listOf("ABC", "空白", "↵"))
            return
        }
        listOf("qwertyuiop", "asdfghjkl", "zxcvbnm").forEachIndexed { index, row ->
            val keys = row.map { if (shift) it.uppercase() else it.toString() }
            if (index == 2) addRow(container, listOf("⇧") + keys + listOf("⌫")) else addRow(container, keys)
        }
        addRow(container, listOf("?123", ",", "空白", ".", "↵"))
    }

    private fun addRow(parent: LinearLayout, labels: List<String>) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        labels.forEach { label ->
            val view = TextView(this).apply {
                text = label
                gravity = Gravity.CENTER
                textSize = if (label.length > 1) 13f else 19f
                setTextColor(Color.rgb(244, 242, 248))
                typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                contentDescription = when (label) {
                    "⌫" -> "Delete previous character"
                    "↵" -> "Enter"
                    "空白" -> "Space"
                    "⇧" -> "Shift"
                    else -> label
                }
                background = GradientDrawable().apply {
                    setColor(if (label in listOf("空白", "⇧", "⌫", "↵", "?123", "ABC")) Color.rgb(62, 58, 76) else Color.rgb(49, 52, 62))
                    cornerRadius = dp(13).toFloat()
                    setStroke(dp(1), Color.argb(34, 255, 255, 255))
                }
                setPadding(dp(3), 0, dp(3), 0)
                isFocusable = true
                isClickable = true
                setOnClickListener { handleKey(label, this) }
            }
            val weight = if (label == "空白") 4f else if (label.length > 1 || label in listOf("⇧","⌫","↵","?123","ABC")) 1.35f else 1f
            row.addView(view, LinearLayout.LayoutParams(0, dp(48), weight).apply {
                setMargins(dp(2), dp(3), dp(2), dp(3))
            })
        }
        parent.addView(row, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(54)))
    }

    private fun handleKey(label: String, view: View) {
        view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
        val connection = currentInputConnection ?: return
        when (label) {
            "⌫" -> connection.deleteSurroundingText(1, 0)
            "↵" -> {
                val action = currentInputEditorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION)
                if (action != null && action != EditorInfo.IME_ACTION_NONE && action != EditorInfo.IME_ACTION_UNSPECIFIED) {
                    connection.performEditorAction(action)
                } else connection.commitText("\n", 1)
            }
            "空白" -> connection.commitText(" ", 1)
            "−" -> connection.commitText("-", 1)
            "⇧" -> { shift = !shift; renderKeys() }
            "?123" -> { numericMode = true; renderKeys() }
            "ABC" -> { numericMode = false; renderKeys() }
            else -> {
                val text = if (label.length == 1) label else return
                connection.commitText(text, 1)
                if (shift) { shift = false; renderKeys() }
            }
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
