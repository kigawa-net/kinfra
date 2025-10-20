/*
 * This is the build configuration for the CLI application module.
 */

plugins {
    application
    id("com.github.johnrengelman.shadow")
}

dependencies {
    implementation(project(":model"))
    implementation(project(":action"))
    implementation(project(":infrastructure"))
}

// Generate version.properties file at build time
val generateVersionProperties by tasks.registering {
    val outputDir = layout.buildDirectory.dir("generated/resources")
    outputs.dir(outputDir)

    doLast {
        val propertiesFile = outputDir.get().file("version.properties").asFile
        propertiesFile.parentFile.mkdirs()
        propertiesFile.writeText("version=${project.version}\n")
    }
}

sourceSets {
    main {
        resources {
            srcDir(generateVersionProperties)
        }
    }
}

tasks.processResources {
    dependsOn(generateVersionProperties)
}

application {
    mainClass = "net.kigawa.kinfra.AppKt"
}

tasks.shadowJar {
    archiveBaseName.set("kinfra-cli")
    archiveClassifier.set("")
    archiveVersion.set(project.version.toString())
    manifest {
        attributes["Main-Class"] = "net.kigawa.kinfra.AppKt"
        attributes["Implementation-Version"] = project.version.toString()
    }
}