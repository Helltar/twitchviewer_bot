plugins {
    kotlin("jvm") version "2.2.21"
    id("com.gradleup.shadow") version "9.2.2"
    application
}

group = "com.helltar"
version = "0.9.9"

repositories {
    mavenCentral()
}

object Versions {
    const val EXPOSED = "1.0.0-rc-3"
    const val R2DBC_POSTGRESQL = "1.1.1.RELEASE"
}

dependencies {
    implementation("com.annimon:tgbots-module:8.0.0") { exclude("org.telegram", "telegrambots-webhook") }
    implementation("com.github.twitch4j:twitch4j:1.25.0")
    implementation("io.github.cdimascio:dotenv-kotlin:6.5.1")

    implementation("org.postgresql:r2dbc-postgresql:${Versions.R2DBC_POSTGRESQL}")
    implementation("org.jetbrains.exposed:exposed-core:${Versions.EXPOSED}")
    implementation("org.jetbrains.exposed:exposed-r2dbc:${Versions.EXPOSED}")
    implementation("org.jetbrains.exposed:exposed-java-time:${Versions.EXPOSED}")

    implementation("io.github.oshai:kotlin-logging-jvm:7.0.13")
    implementation("ch.qos.logback:logback-classic:1.5.21")
}

application {
    mainClass.set("com.helltar.twitchviewerbot.bot.TwitchViewerBot")
}
