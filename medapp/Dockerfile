FROM gradle:8.7-jdk17 AS build
WORKDIR /app
# Copy gradle wrapper and config
COPY build.gradle settings.gradle ./
COPY gradle gradle
COPY gradlew ./
# Make gradlew executable
RUN chmod +x gradlew
# Copy source
COPY src ./src
# Build the application
RUN ./gradlew build -x test --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Copy the built artifact (works for .jar or .war if Spring Boot executable)
COPY --from=build /app/build/libs/*-SNAPSHOT.* app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
