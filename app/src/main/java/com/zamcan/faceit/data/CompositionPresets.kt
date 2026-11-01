package com.zamcan.faceit.data

object CompositionPresets {
    val all = listOf(
        PersonalEnvironment(
            name = "The Stone Guardian",
            background = "Football",
            mainObject = "Lion sculpture",
            material = "Natural stone",
            motion = "Subtle",
            widgetsEnabled = true,
            iconsEnabled = true,
            dialerStyle = "Ceramic Sculpt",
            resourceProfile = "Balanced"
        ),
        PersonalEnvironment(
            name = "Madinah Serenity",
            background = "Islamic architecture",
            mainObject = "Geometric crystal",
            material = "Marble",
            motion = "Static",
            widgetsEnabled = true,
            iconsEnabled = true,
            resourceProfile = "Essential"
        ),
        PersonalEnvironment(
            name = "Islamic Geometry",
            background = "Geometry",
            mainObject = "Geometric crystal",
            material = "Brushed metal",
            motion = "Subtle",
            widgetsEnabled = false,
            iconsEnabled = true,
            resourceProfile = "Balanced"
        )
    )

    fun find(name: String): PersonalEnvironment? = all.firstOrNull { it.name == name }
}
