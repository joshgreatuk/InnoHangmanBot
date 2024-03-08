plugins {
    application
    id("com.github.johnrengelman.shadow") version "7.1.2"
    kotlin("jvm")
}

application.mainClass = "com.innocuous.innohangmanbot.HangmanBot"
group = "com.innocuous"
version = "1.0"

val jdaVersion = "5.0.0-beta.20"
val jacksonVersion = "2.17.0-rc1"
val logbackVersion = "1.2.8"
val jUnitVersion = "5.10.2"

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.dv8tion:JDA:$jdaVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("org.junit.jupiter:junit-jupiter:$jUnitVersion")
    implementation(kotlin("stdlib-jdk8"))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.isIncremental = true

    // Set this to the version of java you want to use,
    // the minimum required for JDA is 1.8
}
kotlin {
    jvmToolchain(17)
}