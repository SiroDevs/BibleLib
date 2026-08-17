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
    namespace = "com.biblelib.core.data"

    defaultConfig {
        buildConfigField(
            "String",
            "PaystackSecret",
            "\"${localProperties.getProperty("PAYSTACK_SECRET_KEY") ?: ""}\""
        )
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    api(project(":core:common"))
    api(project(":core:database"))
    api(project(":core:network"))

    api(libs.androidx.compose.material)

    implementation(platform(libs.jan.tennert.supabase.bom))
    implementation(libs.jan.tennert.supabase.postgrest)
    implementation(libs.androidx.core.ktx)

    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    implementation(libs.squareup.retrofit)
    implementation(libs.squareup.retrofit.gson)
    implementation(libs.squareup.okhttp3.logging)
}
