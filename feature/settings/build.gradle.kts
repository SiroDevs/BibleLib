plugins {
    alias(libs.plugins.biblelib.android.feature)
}

android {
    namespace = "com.biblelib.feature.settings"
}

dependencies {
    implementation(project(":core:data"))

    // Profile photo loading
    implementation(libs.coil.compose)
}
