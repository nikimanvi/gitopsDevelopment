# Multi-stage Dockerfile for Spring Boot Microservices
# Stage 1: Build the application using Maven
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
# Nikitha Devops Learning
# Set working directory
WORKDIR /app

# Copy pom.xml files first (for better layer caching)
COPY pom.xml .

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Create the runtime image
FROM eclipse-temurin:21-jre-alpine

# Set working directory
WORKDIR /app

# Create a non-root user to run the application
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the built JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the default port (can be overridden at runtime)
EXPOSE 8080

# Set JVM options for containerized environments
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
