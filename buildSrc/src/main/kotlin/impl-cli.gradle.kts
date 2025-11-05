import org.gradle.kotlin.dsl.dependencies

plugins {
    id("impl-infra")
}
dependencies {
    implementation(project(":kinfra-infra"))
}