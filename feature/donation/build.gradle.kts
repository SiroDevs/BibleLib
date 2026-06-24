plugins {
    alias(libs.plugins.biblelib.android.feature)
}

android {
    namespace = "com.biblelib.feature.donation"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:data"))
}
