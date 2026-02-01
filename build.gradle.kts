plugins {
    id("java")
    id("com.gradleup.shadow") version "9.3.1"
}

group = "github.renderbr.hytale"
version = "0.3.0"

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
    compileOnly("com.hypixel.hytale:Server:2026.01.27-734d39026")

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
    minimize {
        exclude(dependency("github.renderbr.hytale.models.log:.*"))
    }
    mergeServiceFiles()
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    archiveClassifier.set("all")
    isZip64 = true

    exclude("META-INF/*.SF")
    exclude("META-INF/*.DSA")
    exclude("META-INF/*.RSA")
    exclude("META-INF/maven/**")
    exclude("META-INF/DEPENDENCIES")
    exclude("**/LICENSE*", "**/NOTICE*", "README*", "*.txt", "*.md")
    exclude("**/*.kotlin_metadata")
    exclude("**/*.kotlin_module")
    exclude("**/*.kotlin_builtins")
    exclude("**/java.sql.Driver")
    exclude("META-INF/services/com.fasterxml.jackson.core.*")
    exclude("META-INF/proguard/**")
    exclude("META-INF/versions/**")
    exclude("org/jetbrains/annotations/**")
    exclude("org/intellij/lang/annotations/**")


    exclude(
        "**/win32-x86/**",
        "**/linux-x86/**",
        "**/darwin-x86/**",
        "**/module-info.class",
        "libsqlite3.wasm",
        "**/io/roastedroot/**",
        "**/com/j256/**"
    )

    relocate("org.slf4j", "github.renderbr.hytale.shadow.slf4j")
}

