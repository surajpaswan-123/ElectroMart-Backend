# syntax=docker/dockerfile:1

# --- Build stage ---
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy Maven wrapper + POM first for better layer caching
COPY mvnw mvnw.cmd .mvn/ pom.xml ./

# Copy source
COPY src ./src

# Build (skip tests for faster CI; business logic unchanged)
RUN ./mvnw -q -DskipTests package

# --- Runtime stage ---
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the executable jar built by spring-boot-maven-plugin
COPY --from=build /app/target/*.jar ./app.jar

# Render will route traffic to the container port; app uses server.port=8091
EXPOSE 8091

# Start application
ENTRYPOINT ["java","-jar","/app/app.jar"]

