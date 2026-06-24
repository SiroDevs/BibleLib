plugins {
    alias(libs.plugins.biblelib.android.feature)
    alias(libs.plugins.biblelib.android.library.compose)
}

android {
    namespace = "com.biblelib.feature.listing"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:data"))
}
