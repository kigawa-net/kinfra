plugins {
    id("impl-infra")
}

dependencies {
    // https://mvnrepository.com/artifact/software.amazon.awssdk/s3
    implementation("software.amazon.awssdk:s3:2.38.8")
    // https://mvnrepository.com/artifact/net.openhft/zero-allocation-hashing
    implementation("net.openhft:zero-allocation-hashing:0.27ea1")
}
