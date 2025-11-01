plugins {
    kotlin("jvm") version "2.2.21"
    id("com.gradleup.shadow") version "9.2.2"
    application
}

group = "com.helltar"
version = "0.9.8"

repositories {
    mavenCentral()
}

object Versions {
    const val EXPOSED = "0.61.0"
}

dependencies {
    implementation("com.annimon:tgbots-module:8.0.0") {
        exclude("org.telegram", "telegrambots-webhook")
    }

    implementation("com.github.twitch4j:twitch4j:1.25.0")
    implementation("io.github.cdimascio:dotenv-kotlin:6.5.1")

    runtimeOnly("org.postgresql:postgresql:42.7.3")
    runtimeOnly("org.jetbrains.exposed:exposed-jdbc:${Versions.EXPOSED}")
    implementation("org.jetbrains.exposed:exposed-core:${Versions.EXPOSED}")
    implementation("org.jetbrains.exposed:exposed-java-time:${Versions.EXPOSED}")

    implementation("io.github.oshai:kotlin-logging-jvm:7.0.3")
    implementation("ch.qos.logback:logback-classic:1.5.20")
}

application {
    mainClass.set("com.helltar.twitchviewerbot.bot.TwitchViewerBot")
}
