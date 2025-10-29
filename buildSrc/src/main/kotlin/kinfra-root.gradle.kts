plugins {
    application
    id("com.github.johnrengelman.shadow")
}
allprojects {
    group = "net.kigawa.kinfra"
    version = System.getenv("VERSION") ?: "dev"
    apply(plugin = "common")
}

application {
    mainClass = "net.kigawa.kinfra.AppKt"
}