# Stage 1: Build the JAR using Maven
FROM maven:3.9.2-eclipse-temurin-17 AS build

# Set working directory
WORKDIR /app

# Copy Maven project files
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean install -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:17-jdk-focal

# Set working directory
WORKDIR /app

# Copy the JAR from the build stage
COPY --from=build /app/target/txn-routine-svc-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8080

# Optional JVM options
ENV JAVA_OPTS=""

# Run the app
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
