plugins {
    alias(libs.plugins.biblelib.android.feature)
    alias(libs.plugins.biblelib.android.library.compose)
}

android {
    namespace = "com.biblelib.feature.drafts"
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":feature:song"))
    implementation(libs.androidx.foundation)
}
