package com.biblelib.feature.selection.utils

import com.biblelib.core.network.dtos.BibleInfoDto

data class CountryRef(val id: String, val name: String)

const val UNSPECIFIED_COUNTRY_NAME = "Unspecific"

fun BibleInfoDto.countryRefs(): List<CountryRef> =
    countries
        .map { CountryRef(id = it.id, name = it.name) }
        .ifEmpty { listOf(CountryRef(id = RegionMapper.UNSPECIFIED_COUNTRY_ID, name = UNSPECIFIED_COUNTRY_NAME)) }

fun CountryRef.isUnspecified(): Boolean =
    id.equals(RegionMapper.UNSPECIFIED_COUNTRY_ID, ignoreCase = true) ||
        name.equals(UNSPECIFIED_COUNTRY_NAME, ignoreCase = true)
