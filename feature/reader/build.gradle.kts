plugins {
    alias(libs.plugins.biblelib.android.feature)
    alias(libs.plugins.biblelib.hilt)
}

android {
    namespace = "com.biblelib.feature.reader"
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:casting"))
    implementation(project(":core:database"))
    implementation(project(":core:network"))
    implementation(project(":core:design_system"))
    implementation(libs.androidx.work.runtime)
}
