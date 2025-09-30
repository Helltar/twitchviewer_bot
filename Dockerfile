FROM gradle:8.14-jdk21-alpine AS builder

WORKDIR /app

COPY build.gradle.kts gradle.properties settings.gradle.kts ./
RUN gradle shadowJar -x test --no-daemon
COPY src ./src
RUN gradle shadowJar --no-daemon

FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache ffmpeg py3-pip && \
    pip3 install --no-cache-dir --break-system-packages streamlink

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar twitchviewer_bot.jar

ENTRYPOINT ["java", "-jar", "twitchviewer_bot.jar"]
