/*
 * This is the build configuration for the CLI application module.
 */

plugins {
    application
    id("com.github.johnrengelman.shadow")
    id("impl-cli")
}

dependencies {
    implementation(project(":kinfra-api"))
    implementation(project(":action"))
    implementation(project(":kinfra-infra"))
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
    archiveClassifier.unset()
    archiveVersion.unset()
    manifest {
        attributes["Main-Class"] = "net.kigawa.kinfra.AppKt"
        attributes["Implementation-Version"] = project.version.toString()
    }
}

tasks.distTar {
    dependsOn(tasks.shadowJar)
}

tasks.distZip {
    dependsOn(tasks.shadowJar)
}

tasks.startScripts {
    dependsOn(tasks.shadowJar)
}

tasks.startShadowScripts {
    dependsOn(tasks.jar)
}
