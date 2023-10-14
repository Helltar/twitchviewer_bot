import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.10"
    application
}

group = "com.helltar"
version = "0.2.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation ("com.annimon:tgbots-module:6.5.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.7.3")
    implementation("com.github.twitch4j:twitch4j:1.14.0")

    implementation("org.slf4j:slf4j-api:2.0.6")
    implementation("ch.qos.logback:logback-classic:1.4.6")

    implementation("org.xerial:sqlite-jdbc:3.40.1.0")
    implementation("org.jetbrains.exposed:exposed-core:0.40.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.40.1")
}

application {
    mainClass.set("com.helltar.twitchviewerbot.TwitchViewerBot")
}