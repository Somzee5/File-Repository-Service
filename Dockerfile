# Use a lightweight Java 17 base image
FROM openjdk:17-jdk-slim

# Create a working directory inside the container
WORKDIR /app

# Copy the jar file from target folder to container
COPY target/file-repository-service-0.0.1-SNAPSHOT.jar app.jar

# Expose port 8080 (your Spring Boot app runs here)
EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]
