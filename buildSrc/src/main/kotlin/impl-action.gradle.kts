plugins{
    id("impl-model")
}

dependencies {
    implementation(project(":kinfra-api"))
    testImplementation(kotlin("test"))
    testImplementation(project(":kodel:core"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
}
