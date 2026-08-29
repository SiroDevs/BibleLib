plugins {
    alias(libs.plugins.biblelib.android.library)
    alias(libs.plugins.biblelib.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.biblelib.core.casting"
}

dependencies {
    implementation(project(":core:common"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.cio)
    implementation(libs.ktor.server.websockets)
}
