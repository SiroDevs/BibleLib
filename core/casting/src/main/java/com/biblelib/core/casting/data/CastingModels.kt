package com.biblelib.core.casting.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface CastingState {

    @Serializable
    @SerialName("idle")
    data object Idle : CastingState

    @Serializable
    @SerialName("reading")
    data class Reading(
        val bibleName: String, // primary bible display name, e.g. "King James Version"
        val bookName: String, // e.g. "Genesis"
        val chapterRef: String, // e.g. "Genesis 1"
        val verses: List<String>,
        val indicators: List<String>, // verse numbers, shown alongside each verse
        val currentIndex: Int,
        val multiBibleEnabled: Boolean = false,
        val secondaryBibleNames: List<String> = emptyList(), // other translations shown alongside
    ) : CastingState
}

sealed interface ServerStatus {
    data object Stopped : ServerStatus
    data object Starting : ServerStatus
    data class Running(val url: String?, val port: Int) : ServerStatus
    data class Error(val message: String) : ServerStatus
}

sealed interface HotspotStatus {
    data object Stopped : HotspotStatus
    data object Starting : HotspotStatus
    data class Running(val ssid: String, val password: String?, val isOpen: Boolean) : HotspotStatus
    data class Error(val message: String) : HotspotStatus
}
