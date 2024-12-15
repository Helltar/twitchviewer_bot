plugins {
    kotlin("jvm") version "2.1.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application
}

group = "com.helltar"
version = "0.8.5"

repositories {
    mavenCentral()
}

object Versions {
    const val EXPOSED = "0.52.0"
}

dependencies {
    implementation("com.annimon:tgbots-module:8.0.0") {
        exclude("org.telegram", "telegrambots-webhook")
    }

    implementation("com.github.twitch4j:twitch4j:1.22.0")
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.8.1")

    implementation("org.postgresql:postgresql:42.7.3")
    implementation("org.jetbrains.exposed:exposed-core:${Versions.EXPOSED}")
    implementation("org.jetbrains.exposed:exposed-dao:${Versions.EXPOSED}")
    implementation("org.jetbrains.exposed:exposed-java-time:${Versions.EXPOSED}")
    implementation("org.jetbrains.exposed:exposed-jdbc:${Versions.EXPOSED}")

    implementation("org.slf4j:slf4j-api:2.0.13")
    implementation("ch.qos.logback:logback-classic:1.5.6")
}

application {
    mainClass.set("com.helltar.twitchviewerbot.bot.TwitchViewerBot")
}
