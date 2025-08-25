# --- Stage 1: Build the application using Gradle --- #

# Use an official Gradle image with JDK 17 as the build environment.
# The "alpine" tag is used for a smaller base image.
# The "AS build" syntax names this stage, so we can refer to it later.
FROM gradle:8.5.0-jdk17-alpine AS build

# Set the working directory inside the container.
WORKDIR /home/gradle/project

# Copy the Gradle build files to the container.
# This is done first to leverage Docker's layer caching. If these files don't change,
# Docker won't need to re-download the dependencies on subsequent builds.
COPY build.gradle settings.gradle ./

# Copy the application source code.
COPY src ./src

# Run the Gradle build command to compile the code and create the executable JAR.
# The "--no-daemon" flag is recommended for CI/CD environments.
RUN gradle build --no-daemon


# --- Stage 2: Create the final, lightweight production image --- #

# Use a slim JRE image for the final container.
# This results in a much smaller image size compared to a full JDK,
# which is better for production deployments.
FROM openjdk:17-jre-slim

# Set the working directory for the application.
WORKDIR /app

# Copy the executable JAR file from the "build" stage into the final image.
# This is the only artifact we need from the build stage.
COPY --from=build /home/gradle/project/build/libs/*.jar app.jar

# Expose port 8080, which is the default port for the Spring Boot application.
EXPOSE 8080

# The command to run when the container starts.
# This executes the Spring Boot application.
ENTRYPOINT ["java", "-jar", "app.jar"]
