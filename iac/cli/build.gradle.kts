plugins {
    id("common")
}
dependencies {
    implementation(project(":iac:model"))
    implementation(project(":kinfra-cli"))
    implementation(project(":kinfra-infra"))
    implementation(project(":kinfra-api"))
    implementation(project(":kodel:api"))
    implementation(project(":kodel:core"))
    // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-core
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
}