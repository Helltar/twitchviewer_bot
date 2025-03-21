plugins {
    kotlin("jvm") version "2.1.20"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application
}

group = "com.helltar"
version = "0.9.7"

repositories {
    mavenCentral()
}

object Versions {
    const val EXPOSED = "0.60.0"
}

dependencies {
    implementation("com.annimon:tgbots-module:8.0.0") { exclude("org.telegram", "telegrambots-webhook") }
    implementation("com.github.twitch4j:twitch4j:1.24.0")
    implementation("io.github.cdimascio:dotenv-kotlin:6.5.0")

    runtimeOnly("org.postgresql:postgresql:42.7.3")
    runtimeOnly("org.jetbrains.exposed:exposed-jdbc:${Versions.EXPOSED}")
    implementation("org.jetbrains.exposed:exposed-core:${Versions.EXPOSED}")
    implementation("org.jetbrains.exposed:exposed-java-time:${Versions.EXPOSED}")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.10.1")

    implementation("io.github.oshai:kotlin-logging-jvm:7.0.3")
    implementation("ch.qos.logback:logback-classic:1.5.16")
}

application {
    mainClass.set("com.helltar.twitchviewerbot.bot.TwitchViewerBot")
}
