plugins {
    alias(libs.plugins.biblelib.android.feature)
    alias(libs.plugins.biblelib.android.library.compose)
}

android {
    namespace = "com.biblelib.feature.song"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:data"))
    implementation(project(":feature:home"))
}
