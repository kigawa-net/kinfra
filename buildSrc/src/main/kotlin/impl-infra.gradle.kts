plugins{
    id("impl-action")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(project(":action"))
    implementation(project(":kodel:core"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("com.charleskorn.kaml:kaml:0.55.0")
    implementation("com.google.code.gson:gson:2.10.1")
    // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-core
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    // https://mvnrepository.com/artifact/software.amazon.awssdk/s3
    implementation("software.amazon.awssdk:s3:2.38.8")
    // https://mvnrepository.com/artifact/net.openhft/zero-allocation-hashing
    implementation("net.openhft:zero-allocation-hashing:0.27ea1")
    // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-io-core
    implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.8.0")
}
