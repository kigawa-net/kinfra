import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm")
    // id("org.jlleitschuh.gradle.ktlint") // Temporarily disabled
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    testImplementation(kotlin("test"))

    // https://mvnrepository.com/artifact/org.jetbrains.kotlin/kotlin-test-junit5
//    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:2.3.0")
//    testImplementation("org.junit.jupiter:junit-jupiter-engine:6.0.1")
//    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

//java {
//    toolchain {
//        languageVersion = JavaLanguageVersion.of(21)
//    }
//}
//
//tasks.named<Test>("test") {
//    useJUnitPlatform()
//}
val compileKotlin: KotlinCompile by tasks
compileKotlin.compilerOptions {
    freeCompilerArgs.set(listOf("-Xcontext-parameters"))
}

// Configure ktlint - Temporarily disabled