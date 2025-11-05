plugins{
    id("impl-model")
}

dependencies {
    implementation(project(":model"))
    testImplementation(kotlin("test"))
    testImplementation(project(":kodel:core"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
}
