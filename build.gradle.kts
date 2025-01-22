plugins {
    kotlin("jvm") version "2.1.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application
}

group = "com.helltar"
version = "1.0.0"

repositories {
    mavenCentral()
}

object Versions {
    const val EXPOSED = "0.58.0"
}

dependencies {
    implementation("com.annimon:tgbots-module:8.0.0") {
        exclude("org.telegram", "telegrambots-webhook")
    }

    implementation("com.github.twitch4j:twitch4j:1.23.0")
    implementation("io.github.cdimascio:dotenv-kotlin:6.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.10.1")
    implementation("org.jetbrains.exposed:exposed-core:${Versions.EXPOSED}")
    implementation("org.jetbrains.exposed:exposed-java-time:${Versions.EXPOSED}")

    runtimeOnly("org.jetbrains.exposed:exposed-jdbc:${Versions.EXPOSED}")
    runtimeOnly("org.postgresql:postgresql:42.7.3")

    implementation("io.github.oshai:kotlin-logging-jvm:7.0.3")
    implementation("ch.qos.logback:logback-classic:1.5.16")
}

application {
    mainClass.set("com.helltar.twitchviewerbot.bot.TwitchViewerBot")
}
