plugins {
    alias(libs.plugins.biblelib.android.feature)
    alias(libs.plugins.biblelib.android.library.compose)
}

android {
    namespace = "com.biblelib.feature.casting"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:casting"))
}
