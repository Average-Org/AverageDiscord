plugins {
    id("java")
    id("com.gradleup.shadow") version "9.3.1"
}

group = "github.renderbr.hytale"
version = "0.2.4"

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

tasks.test {
    useJUnitPlatform()
}

tasks.shadowJar {
    minimize()
    mergeServiceFiles()
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    archiveClassifier.set("all")
    isZip64 = true

    exclude("META-INF/*.SF")
    exclude("META-INF/*.DSA")
    exclude("META-INF/*.RSA")
    exclude("META-INF/maven/**")
    exclude("META-INF/DEPENDENCIES")
    exclude("LICENSE*", "NOTICE*", "README*", "*.txt", "*.md")


    exclude(
        "**/win32-x86/**",
        "**/linux-x86/**",
        "**/darwin-x86/**",
        "**/module-info.class"
    )
}
