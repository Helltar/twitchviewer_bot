plugins {
    kotlin("jvm") version "1.9.23"
    application
}

group = "com.helltar"
version = "0.2.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation ("com.annimon:tgbots-module:7.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.8.0")
    implementation("com.github.twitch4j:twitch4j:1.18.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")

    implementation("org.slf4j:slf4j-api:2.0.6")
    implementation("ch.qos.logback:logback-classic:1.4.14")

    implementation("com.h2database:h2:2.1.214")
    implementation("org.jetbrains.exposed:exposed-java-time:0.44.1")
    implementation("org.jetbrains.exposed:exposed-core:0.44.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.44.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.44.1")
}

application {
    mainClass.set("com.helltar.twitchviewerbot.TwitchViewerBot")
}