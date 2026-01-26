plugins {
    id("java")
    id("com.gradleup.shadow") version "9.3.1"
}

group = "github.renderbr.hytale"
version = "0.2.3"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    compileOnly(files("libs/HytaleServer.jar"))

    implementation(files("libs/AverageHytaleCore.jar"))
    implementation("net.dv8tion:JDA:6.3.0") {}

}

tasks.test {
    useJUnitPlatform()
}
