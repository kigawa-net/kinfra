plugins{
//    id("impl-model")
    id("jvm")
}

dependencies {
    api(project(":kinfra-api"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
    testImplementation(project(":kodel:core"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
}
