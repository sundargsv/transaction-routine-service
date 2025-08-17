# Use Eclipse Temurin JDK 17 as base image
FROM eclipse-temurin:17-jdk-focal AS build

# Set working directory
WORKDIR /app

# Copy the built jar file (assuming Maven build target)
COPY target/txn-routine-svc-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8080

# JVM options (optional)
ENV JAVA_OPTS=""

# Run the app
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
