package com.zamcan.faceit.messaging

/**
 * Estimates standard SMS text encoding and concatenated-message segments.
 *
 * GSM-7 extension-table characters consume two septets. Any character outside
 * the GSM default/extension alphabets switches the entire message to UCS-2
 * (UTF-16 code units), as with common carrier SMS implementations.
 *
 * This is an estimate: carrier gateways and device messaging apps may apply
 * additional transport-specific behaviour.
 */
object SmsSegmentEstimator {
    enum class Encoding { GSM_7, UCS_2 }

    data class Estimate(
        val encoding: Encoding,
        val units: Int,
        val segments: Int,
        val singleSegmentCapacity: Int,
        val concatenatedSegmentCapacity: Int
    )

    private const val GSM_SINGLE = 160
    private const val GSM_CONCAT = 153
    private const val UCS_SINGLE = 70
    private const val UCS_CONCAT = 67

    private val gsmBasic = (
        "@£$¥èéùìòÇ\nØø\rÅåΔ_ΦΓΛΩΠΨΣΘΞÆæßÉ " +
            "!\"#¤%&'()*+,-./0123456789:;<=>?¡" +
            "ABCDEFGHIJKLMNOPQRSTUVWXYZÄÖÑÜ§¿" +
            "abcdefghijklmnopqrstuvwxyzäöñüà"
        ).toSet()

    // Each extension-table character is transmitted as ESC + character.
    private val gsmExtension = setOf('\u000c', '^', '{', '}', '\\', '[', '~', ']', '|', '€')

    fun estimate(text: CharSequence): Estimate {
        if (text.isEmpty()) return Estimate(Encoding.GSM_7, 0, 0, GSM_SINGLE, GSM_CONCAT)

        var septets = 0
        var isGsm = true
        var index = 0
        while (index < text.length) {
            val codePoint = Character.codePointAt(text, index)
            val character = String(Character.toChars(codePoint))
            when {
                codePoint <= Char.MAX_VALUE.code && character[0] in gsmBasic -> septets += 1
                codePoint <= Char.MAX_VALUE.code && character[0] in gsmExtension -> septets += 2
                else -> {
                    isGsm = false
                    break
                }
            }
            index += Character.charCount(codePoint)
        }

        return if (isGsm) {
            val segments = if (septets <= GSM_SINGLE) 1 else (septets + GSM_CONCAT - 1) / GSM_CONCAT
            Estimate(Encoding.GSM_7, septets, segments, GSM_SINGLE, GSM_CONCAT)
        } else {
            // UTF-16 code units: supplementary characters such as emoji use two.
            val units = text.length
            val segments = if (units <= UCS_SINGLE) 1 else (units + UCS_CONCAT - 1) / UCS_CONCAT
            Estimate(Encoding.UCS_2, units, segments, UCS_SINGLE, UCS_CONCAT)
        }
    }
}
