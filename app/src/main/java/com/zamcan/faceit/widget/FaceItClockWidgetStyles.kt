package com.zamcan.faceit.widget

data class FaceItClockStyle(
    val id: String,
    val label: String,
    val background: Int,
    val time: Int,
    val date: Int
)

object FaceItClockWidgetStyles {
    val all = listOf(
        FaceItClockStyle("Obsidian", "Obsidian Glass", 0xE622252D.toInt(), 0xFFF4F0FA.toInt(), 0xFFA78BFA.toInt()),
        FaceItClockStyle("Ceramic", "Warm Ceramic", 0xEAE7E0D3.toInt(), 0xFF292A2D.toInt(), 0xFF78664D.toInt()),
        FaceItClockStyle("Mint", "Pearl Mint", 0xE6233737.toInt(), 0xFFF2FAF6.toInt(), 0xFF76DCCB.toInt())
    )

    fun find(id: String?): FaceItClockStyle = all.firstOrNull { it.id == id } ?: all.first()
}
