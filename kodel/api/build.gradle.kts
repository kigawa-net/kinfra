@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest

plugins {
    kotlin("multiplatform")
}
repositories {
    mavenCentral()
    gradlePluginPortal()
}
fun KotlinJsTest.browserTest() {
    val firefox = providers.gradleProperty("useFirefox")
        .map { it.toBoolean() }
        .getOrElse(true)
    val chrome = providers.gradleProperty("useChrome")
        .map { it.toBoolean() }
        .getOrElse(true)
    enabled = firefox || chrome
    useKarma {
        if (firefox) useFirefoxHeadless()
        if (chrome) useChromeHeadlessNoSandbox()
    }
}
kotlin {
    compilerOptions {
        freeCompilerArgs = listOf("-Xcontext-parameters")
    }
    jvm {}
    js {
        browser {
            testTask {
                browserTest()
            }
        }
    }
    wasmJs {
        browser {
            testTask {
                enabled
                browserTest()
            }
        }
    }
    sourceSets {
        commonMain {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1-0.6.x-compat")
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
            }
        }
    }
}
