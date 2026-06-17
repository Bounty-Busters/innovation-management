# syntax=docker/dockerfile:1.7
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# Keep Gradle's wrapper distribution and dependency caches across Docker builds.
ENV GRADLE_USER_HOME=/root/.gradle

# Copy Gradle wrapper and build configuration first so dependency resolution stays cacheable.
COPY build.gradle settings.gradle ./
COPY gradle gradle
COPY gradlew ./

RUN chmod +x gradlew

# Warm the Gradle wrapper and dependency cache before source changes invalidate later layers.
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew dependencies -x test --no-daemon

# Copy source separately so code changes do not invalidate dependency download layers.
COPY src ./src

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew build -x test --no-daemon

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
# Copy the built artifact (works for .jar or .war if Spring Boot executable)
COPY --from=build /app/build/libs/*-SNAPSHOT.* app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
