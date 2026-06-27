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

<<<<<<< HEAD
ENTRYPOINT ["java","-jar","app.jar"]
=======
ENTRYPOINT ["java","-jar","app.jar"]
>>>>>>> 47112cdc32bb348708f0eccd984ba5a67b371464
