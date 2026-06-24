plugins {
    alias(libs.plugins.biblelib.android.feature)
    alias(libs.plugins.biblelib.android.library.compose)
}

android {
    namespace = "com.biblelib.feature.help"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:data"))
}
