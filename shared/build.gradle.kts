import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    android {
        namespace = "net.onefivefour.crumbandcrust.shared"
        compileSdk = 37
        minSdk = 24
        androidResources { enable = true }
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs { browser() }

    // JVM target is used for fast common-domain tests, not a desktop application.
    jvm()

    sourceSets {
        commonTest.dependencies { implementation(kotlin("test")) }
        commonMain.dependencies {
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.coroutines)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.compose.resources)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)
        }
    }
}

compose.resources { packageOfResClass = "net.onefivefour.crumbandcrust.resources" }
