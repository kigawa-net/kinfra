/*
 * This is the build configuration for the CLI application module.
 */

plugins {
    application
    id("com.gradleup.shadow")
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

// 通常のjarタスクとshadowJarタスクが同一ファイル名(kinfra-cli-<version>.jar)を
// 生成すると、後から実行された方が他方を上書きしてしまう。plainなjarにはclassifierを
// 付けて分離し、install.sh/release.ymlが期待する非classifierの名前は
// 常にshadowJar(全依存を含む実行可能jar)が生成するようにする。
tasks.jar {
    archiveClassifier.set("thin")
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

tasks.distTar {
    dependsOn(tasks.shadowJar)
}

tasks.distZip {
    dependsOn(tasks.shadowJar)
}
