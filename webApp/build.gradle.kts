import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName.set("crumb-and-crust")
        browser {
            commonWebpackConfig {
                outputFileName = "crumb-and-crust.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    port = 8080
                    open = false
                }
            }
        }
        binaries.executable()
    }
    sourceSets {
        wasmJsMain.dependencies {
            implementation(npm("@js-joda/timezone", "2.25.2"))
            implementation(libs.kotlinx.coroutines)
            implementation(project(":shared"))
            implementation(libs.compose.ui)
        }
    }
}
