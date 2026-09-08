plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "net.onefivefour.crumbandcrust"
    compileSdk = 37

    defaultConfig {
        applicationId = "net.onefivefour.crumbandcrust"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures { compose = true }
}

dependencies {
    coreLibraryDesugaring(libs.android.desugar)
    implementation(project(":shared"))
    implementation(libs.androidx.activity.compose)
}
