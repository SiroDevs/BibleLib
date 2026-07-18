plugins {
    alias(libs.plugins.biblelib.android.feature)
    alias(libs.plugins.biblelib.hilt)
}

android {
    namespace = "com.biblelib.feature.settings"
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:database"))
    implementation(project(":core:network"))
    implementation(project(":core:designsystem"))
    implementation(libs.androidx.work.runtime)
}
