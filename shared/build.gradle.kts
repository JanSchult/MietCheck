// shared/build.gradle.kts
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.android.lint)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.compilerCompose)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {

    androidLibrary {
        namespace = "com.example.shared"
        compileSdk = 36
        minSdk = 26

        withHostTestBuilder {
        }

        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    val xcfName = "sharedKit"

    iosX64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    iosArm64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    iosSimulatorArm64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.stdlib)
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)

                // Koin
                implementation(libs.koin.core)
                implementation(libs.koin.compose)
                implementation(libs.koin.compose.viewmodel)

                // SQLDelight
                implementation(libs.sqldelight.runtime)
                implementation(libs.sqldelight.coroutines)

                // Compose Multiplatform UI
                implementation(libs.lifecycle.viewmodel.core)
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(libs.navigation.compose)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.androidx.biometric)
                implementation(libs.play.services.mlkit.text.recognition)
                implementation(libs.play.services.mlkit.text.recognition.common)
                implementation(libs.koin.android)

                // SQLDelight + SQLCipher (verschlüsselte Datenbank)
                implementation(libs.sqldelight.android)
                implementation("net.zetetic:sqlcipher-android:4.5.6@aar")   // Version auf aktuelle 4.x prüfen
                implementation(libs.androidx.sqlite)                        // androidx.sqlite:sqlite, nicht "bundled"
            }
        }

        getByName("androidDeviceTest") {
            dependencies {
                implementation(libs.androidx.runner)
                implementation(libs.androidx.core)
                implementation(libs.androidx.junit)
            }
        }

        iosMain {
            dependencies {
                implementation(libs.sqldelight.native)
            }
        }
    }
}

sqldelight {
    databases {
        create("MietCheckDatabase") {
            packageName.set("com.example.shared.data.local")
        }
    }
}