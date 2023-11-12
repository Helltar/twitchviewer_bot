plugins {
    kotlin("jvm") version "1.9.20"
    application
}

group = "com.helltar"
version = "0.2.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation ("com.annimon:tgbots-module:6.5.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.7.3")
    implementation("com.github.twitch4j:twitch4j:1.18.0")
    implementation("com.google.code.gson:gson:2.10.1")

    implementation("org.slf4j:slf4j-api:2.0.6")
    implementation("ch.qos.logback:logback-classic:1.4.7")

    implementation("com.h2database:h2:2.1.214")
    implementation("org.jetbrains.exposed:exposed-java-time:0.44.1")
    implementation("org.jetbrains.exposed:exposed-core:0.44.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.44.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.44.1")
}

application {
    mainClass.set("com.helltar.twitchviewerbot.TwitchViewerBot")
}