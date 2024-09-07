plugins {
    id("kotlin-kapt")
}

dependencies {
    // Telegraff
    api(project(":telegraff-core"))

    // Spring
    implementation("org.springframework.boot:spring-boot-autoconfigure")

    // DevTools
    kapt("org.springframework.boot:spring-boot-configuration-processor")
}