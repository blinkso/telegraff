dependencies {
    implementation(project(":telegraff-core"))
    api(project(":telegraff-autoconfigure"))

    // Spring
    api("org.springframework.boot:spring-boot-starter-webflux")

    // Kotlin
    api("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    api("org.jetbrains.kotlin:kotlin-reflect")
    api("org.jetbrains.kotlin:kotlin-script-util")
    api("org.jetbrains.kotlin:kotlin-compiler-embeddable")
    api("org.jetbrains.kotlin:kotlin-scripting-compiler-embeddable")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    api("net.java.dev.jna:jna:5.11.0")
}