package com.zamcan.faceit.data
data class VisualCategory(val title: String, val description: String, val collection: String)
object VisualCatalogue {
    val categories = listOf(
        VisualCategory("Islamic cities & heritage", "Makkah, Madinah, Al-Quds, mosques and Islamic art", "FIRST PRIORITY"),
        VisualCategory("Football & sport", "Stadiums, football culture, teams and sporting objects", "SPORT"),
        VisualCategory("Health & medicine", "Stethoscopes, medical instruments and anatomy-inspired art", "DISCIPLINE"),
        VisualCategory("Engineering & architecture", "Blueprints, structures, mechanisms and industrial design", "DISCIPLINE"),
        VisualCategory("Law & justice", "Legal architecture, books and classical legal symbolism", "DISCIPLINE"),
        VisualCategory("Programming & technology", "Code, computing, circuits and digital craft", "DISCIPLINE"),
        VisualCategory("Finance & economics", "Accounting, financial instruments and economic forms", "DISCIPLINE"),
        VisualCategory("Science & research", "Laboratories, chemistry, physics and materials", "DISCIPLINE"),
        VisualCategory("Nature & wildlife", "Animals, landscapes, oceans and botanical forms", "NATURE"),
        VisualCategory("Automotive & transport", "Cars, aviation, ships and industrial mobility", "DESIGN"),
        VisualCategory("Fashion & luxury", "Jewellery, textiles, accessories and premium objects", "DESIGN"),
        VisualCategory("Geometry & abstract", "Crystals, sculptural forms and original abstract art", "ART"),
        VisualCategory("Cities & world cultures", "Architecture, streets and places around the world", "CULTURE")
    )
}
