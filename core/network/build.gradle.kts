import java.util.Properties

plugins {
    alias(libs.plugins.biblelib.android.library)
    alias(libs.plugins.biblelib.hilt)
}

val localProperties = Properties()
val localFile = rootProject.file("local.properties")
if (localFile.exists()) {
    localProperties.load(localFile.inputStream())
}

android {
    namespace = "com.biblelib.core.network"
}

dependencies {
    api(project(":core:common"))
    api(project(":core:database"))

    implementation(libs.squareup.retrofit)
    implementation(libs.squareup.retrofit.gson)
    implementation(libs.squareup.okhttp3.logging)
}
