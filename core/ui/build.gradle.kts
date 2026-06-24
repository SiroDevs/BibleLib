plugins {
    alias(libs.plugins.biblelib.android.library.compose)
    alias(libs.plugins.biblelib.hilt)
}

android {
    namespace = "com.biblelib.core.ui"
}

dependencies {
    api(project(":core:common"))
    api(project(":core:database"))
    api(project(":core:designsystem"))

    implementation(libs.androidx.compose.livedata)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.foundation)
    implementation(libs.hilt.android)
}
