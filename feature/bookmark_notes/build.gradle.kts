plugins {
    alias(libs.plugins.biblelib.android.feature)
    alias(libs.plugins.biblelib.hilt)
}

android {
    namespace = "com.biblelib.feature.bookmark_notes"
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:database"))
    implementation(project(":core:network"))
}
