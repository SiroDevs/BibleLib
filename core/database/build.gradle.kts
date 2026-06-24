plugins {
    alias(libs.plugins.biblelib.android.library)
    alias(libs.plugins.biblelib.hilt)
    alias(libs.plugins.devtools.ksp)
    id("kotlin-parcelize")
}

android {
    namespace = "com.biblelib.core.database"
}

dependencies {
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
}
