package com.biblelib.core.ui.sample

import com.biblelib.core.database.model.BibleEntity
import com.biblelib.core.common.entity.Selectable

val SampleBibles = listOf(
    BibleEntity(
        BibleId = 1,
        user = 1,
        title = "Songs of Worship",
        subTitle = "worship",
        songs = 750,
        position = 1,
        BibleNo = 1,
        enabled = true,
        created = ""
    ),
    BibleEntity(
        BibleId = 2,
        user = 1,
        title = "Nyimbo za Injili",
        subTitle = "injili",
        songs = 213,
        position = 2,
        BibleNo = 2,
        enabled = true,
        created = ""
    ),
    BibleEntity(
        BibleId = 3,
        user = 1,
        title = "Redemption Songs",
        subTitle = "redemption",
        songs = 712,
        position = 3,
        BibleNo = 3,
        enabled = true,
        created = ""
    )
)

val SampleSelectableBibles = listOf(
    Selectable(
        BibleEntity(
            BibleId = 1,
            BibleNo = 1,
            created = "",
            enabled = true,
            position = 1,
            songs = 750,
            subTitle = "worship",
            title = "Songs of Worship",
            user = 1
        ),
    ),
    Selectable(
        BibleEntity(
            BibleId = 2,
            BibleNo = 2,
            created = "",
            enabled = true,
            position = 2,
            songs = 220,
            subTitle = "injili",
            title = "Nyimbo za Injili",
            user = 1
        ),
        isSelected = true
    ),
    Selectable(
        BibleEntity(
            BibleId = 3,
            BibleNo = 3,
            created = "",
            enabled = true,
            position = 2,
            songs = 600,
            subTitle = "kikuyu",
            title = "Nyimbo cia Kunira Ngai",
            user = 1
        ),
        isSelected = true
    ),
    Selectable(
        BibleEntity(
            BibleId = 4,
            BibleNo = 4,
            created = "",
            enabled = true,
            position = 4,
            songs = 200,
            subTitle = "gusii",
            title = "Amatero Y'enchiri",
            user = 1
        ),
        isSelected = false
    ),
)