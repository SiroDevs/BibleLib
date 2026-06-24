plugins {
    alias(libs.plugins.biblelib.android.library)
    id("kotlin-parcelize")
}

android {
    namespace = "com.biblelib.core.common"
}

dependencies {
    api(project(":core:database"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.ktor.client.android)
}
