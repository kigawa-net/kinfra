import org.jetbrains.kotlin.gradle.dsl.JvmTarget
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

java {
    sourceCompatibility = JavaVersion.VERSION_24
    targetCompatibility = JavaVersion.VERSION_24
}

//tasks.named<Test>("test") {
//    useJUnitPlatform()
//}
val compileKotlin: KotlinCompile by tasks
compileKotlin.compilerOptions {
    jvmTarget = JvmTarget.JVM_24
    freeCompilerArgs.set(listOf("-Xcontext-parameters"))
}

// Configure ktlint - Temporarily disabled