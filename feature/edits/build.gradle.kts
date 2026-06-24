plugins {
    alias(libs.plugins.biblelib.android.feature)
    alias(libs.plugins.biblelib.android.library.compose)
}

android {
    namespace = "com.biblelib.feature.edits"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:data"))
    implementation(libs.androidx.foundation)
}
