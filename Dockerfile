# syntax=docker/dockerfile:1

# ---------- Build Stage ----------
FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy project
COPY . .

# Make Maven Wrapper executable
RUN chmod +x mvnw

# Build project
RUN ./mvnw clean package -DskipTests

# ---------- Runtime Stage ----------
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8091

ENTRYPOINT ["java","-jar","app.jar"]