plugins {
    id("java")
    id("com.gradleup.shadow") version "9.3.1"
}

group = "github.renderbr.hytale"
version = "0.4.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.hytale.com/release")
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    compileOnly("com.hypixel.hytale:Server:2026.02.19-1a311a592")

    implementation(files("libs/AverageHytaleCore.jar"))
    implementation("net.dv8tion:JDA:6.3.0") {
        exclude(module = "opus-java")
        exclude(module = "tink")
    }
}

configurations.all {
    // Prevents conflicts with spark logging
    exclude(group = "org.slf4j", module = "slf4j-log4j12")
    exclude(group = "org.slf4j", module = "slf4j-jdk14")
}

tasks.test {
    useJUnitPlatform()
}

tasks.shadowJar {
    mergeServiceFiles()
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    isZip64 = true

    relocate("org.slf4j", "github.renderbr.hytale.shadow.slf4j")
}

