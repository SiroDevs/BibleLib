package com.biblelib.feature.selection.utils

enum class GroupingMode(val label: String) {
    REGIONS("Regions"),
    COUNTRIES("Countries"),
    LANGUAGES("Languages"),
    NONE("None");

    companion object {
        val Default = REGIONS
    }
}
