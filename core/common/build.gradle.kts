plugins {
    alias(libs.plugins.biblelib.android.library)
}

android {
    namespace = "com.biblelib.core.common"
}

dependencies {
    api(project(":core:database"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.ktor.client.android)
}
