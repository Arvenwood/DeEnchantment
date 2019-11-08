plugins {
    id("java")
    id("org.spongepowered.plugin") version "0.9.0"
}

group = "com.arvenwood"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.spongepowered:spongeapi:7.1.0")
    annotationProcessor("org.spongepowered:spongeapi:7.1.0")
}