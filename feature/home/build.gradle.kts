plugins {
    alias(libs.plugins.biblelib.android.feature)
    alias(libs.plugins.biblelib.android.library.compose)
}

android {
    namespace = "com.biblelib.feature.home"
}

dependencies {
    implementation(project(":core:data"))
    implementation(libs.androidx.foundation)

    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)
}
