plugins {
    alias(libs.plugins.biblelib.android.feature)
    alias(libs.plugins.biblelib.hilt)
}

android {
    namespace = "com.biblelib.feature.search"
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:database"))
    implementation(project(":core:network"))
}
