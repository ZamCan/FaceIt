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
 * Offline native input method. It does not persist, inspect, or transmit typed content.
 * Users enable and select it themselves through Android's input-method settings.
 */
class FaceItKeyboardService : InputMethodService() {
    private var numericMode = false
    private var shift = false
    private var root: LinearLayout? = null
    private var keySurface = Color.rgb(49, 52, 62)
    private var specialSurface = Color.rgb(62, 58, 76)
    private var keyText = Color.rgb(244, 242, 248)
    private var keyStrokeColor = Color.argb(34, 255, 255, 255)
    private var keyRadiusDp = 13
    private var keyTypeface = "sans-serif-medium"
    private var hapticsEnabled = true

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
        }
        renderKeys()
        return root!!
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        renderKeys()
    }

    private fun renderKeys() {
        val prefs = getSharedPreferences("faceit_keyboard", MODE_PRIVATE)
        val palette = prefs.getString("palette", "OBSIDIAN_CERAMIC")
        val colors = when (palette) {
            "WARM_STONE" -> intArrayOf(Color.rgb(81, 70, 60), Color.rgb(103, 83, 68), Color.rgb(245, 235, 221))
            "PEARL_MINT" -> intArrayOf(Color.WHITE, Color.rgb(225, 235, 229), Color.rgb(37, 43, 41))
            "GRAPHITE_ICE" -> intArrayOf(Color.rgb(48, 58, 67), Color.rgb(65, 77, 88), Color.rgb(240, 244, 247))
            else -> intArrayOf(Color.rgb(49, 52, 62), Color.rgb(62, 58, 76), Color.rgb(244, 242, 248))
        }
        keySurface = colors[0]
        specialSurface = colors[1]
        keyText = colors[2]
        keyStrokeColor = when (prefs.getString("material", "MATTE_CERAMIC")) {
            "FROSTED_GLASS" -> Color.argb(115, 225, 239, 255)
            "NATURAL_STONE" -> Color.rgb(145, 125, 108)
            "BRUSHED_METAL" -> Color.rgb(137, 149, 164)
            "SOFT_MATTE" -> Color.argb(22, 255, 255, 255)
            else -> Color.argb(34, 255, 255, 255)
        }
        keyRadiusDp = when (prefs.getString("shape", "ROUNDED_RECTANGLE")) {
            "PILL" -> 28
            "SOFT_SQUARE" -> 7
            "CIRCULAR" -> 30
            else -> 13
        }
        keyTypeface = when (prefs.getString("font", "SYSTEM")) {
            "SERIF" -> "serif"
            "MONOSPACE" -> "monospace"
            "ROUNDED" -> "sans-serif-rounded"
            "SANS" -> "sans-serif"
            else -> "sans-serif-medium"
        }
        hapticsEnabled = prefs.getBoolean("haptics", true)
        val container = root ?: return
        container.removeAllViews()
        container.setBackgroundColor(
            when (palette) {
                "PEARL_MINT" -> Color.rgb(233, 236, 233)
                "WARM_STONE" -> Color.rgb(40, 35, 31)
                else -> Color.rgb(23, 25, 31)
            }
        )

        if (numericMode) {
            listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("-", "0", "⌫")
            ).forEach { addRow(container, it) }
            addRow(container, listOf("ABC", "Space", "↵"))
            return
        }

        val rows = when (prefs.getString("layout", "QWERTY")) {
            "QWERTZ" -> listOf("qwertzuiop", "asdfghjkl", "yxcvbnm")
            "AZERTY" -> listOf("azertyuiop", "qsdfghjklm", "wxcvbn")
            else -> listOf("qwertyuiop", "asdfghjkl", "zxcvbnm")
        }
        rows.forEachIndexed { index, row ->
            val keys = row.map { if (shift) it.uppercase() else it.toString() }
            if (index == 2) addRow(container, listOf("⇧") + keys + listOf("⌫"))
            else addRow(container, keys)
        }
        addRow(container, listOf("?123", ",", "Space", ".", "↵"))
    }

    private fun addRow(parent: LinearLayout, labels: List<String>) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        labels.forEach { label ->
            val special = label in listOf("Space", "⇧", "⌫", "↵", "?123", "ABC")
            val view = TextView(this).apply {
                text = if (label == "Space") "" else label
                gravity = Gravity.CENTER
                textSize = if (label.length > 1) 13f else 19f
                setTextColor(keyText)
                typeface = Typeface.create(keyTypeface, Typeface.NORMAL)
                contentDescription = when (label) {
                    "⌫" -> "Delete previous character"
                    "↵" -> "Enter"
                    "Space" -> "Space"
                    "⇧" -> "Shift"
                    else -> label
                }
                background = GradientDrawable().apply {
                    setColor(if (special) specialSurface else keySurface)
                    cornerRadius = dp(keyRadiusDp).toFloat()
                    setStroke(dp(1), keyStrokeColor)
                }
                setPadding(dp(3), 0, dp(3), 0)
                isFocusable = true
                isClickable = true
                setOnClickListener { handleKey(label, this) }
            }
            val weight = when (label) {
                "Space" -> 4f
                "⇧", "⌫", "↵", "?123", "ABC" -> 1.35f
                else -> 1f
            }
            row.addView(view, LinearLayout.LayoutParams(0, dp(48), weight).apply {
                setMargins(dp(2), dp(3), dp(2), dp(3))
            })
        }
        parent.addView(row, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(54)))
    }

    private fun handleKey(label: String, view: View) {
        if (hapticsEnabled) view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
        val connection = currentInputConnection ?: return
        when (label) {
            "⌫" -> connection.deleteSurroundingText(1, 0)
            "↵" -> {
                val action = currentInputEditorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION)
                if (action != null && action != EditorInfo.IME_ACTION_NONE &&
                    action != EditorInfo.IME_ACTION_UNSPECIFIED
                ) connection.performEditorAction(action)
                else connection.commitText("\n", 1)
            }
            "Space" -> connection.commitText(" ", 1)
            "⇧" -> {
                shift = !shift
                renderKeys()
            }
            "?123" -> {
                numericMode = true
                renderKeys()
            }
            "ABC" -> {
                numericMode = false
                renderKeys()
            }
            else -> {
                if (label.length != 1) return
                connection.commitText(label, 1)
                if (shift) {
                    shift = false
                    renderKeys()
                }
            }
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
