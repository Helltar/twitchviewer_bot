val exposed_version: String by project
val postgresql_driver_version: String by project

plugins {
    kotlin("jvm") version "2.0.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application
}

group = "com.helltar"
version = "0.7.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.annimon:tgbots-module:7.7.0") {
        exclude("org.telegram", "telegrambots-webhook")
    }

    implementation("com.github.twitch4j:twitch4j:1.20.0")
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.8.1")

    implementation("org.postgresql:postgresql:$postgresql_driver_version")
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-dao:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")

    implementation("org.slf4j:slf4j-api:2.0.13")
    implementation("ch.qos.logback:logback-classic:1.5.6")
}

application {
    mainClass.set("com.helltar.twitchviewerbot.bot.TwitchViewerBot")
}