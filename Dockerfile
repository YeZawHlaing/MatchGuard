# ==========================================
# Stage 1: Build the Application
# ==========================================
FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /app

# Copy Maven wrapper and configuration files first to cache dependencies
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw

# Download dependencies (cached if pom.xml hasn't changed)
RUN ./mvnw dependency:go-offline -B

# Copy the actual source code
COPY src ./src

# Build the shaded/packaged jar skipping tests for speed
RUN ./mvnw clean package -DskipTests

# ==========================================
# Stage 2: Production Runtime Image
# ==========================================
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Create a non-root system user for enhanced container security
RUN useradd -ms /bin/bash matchguard && chown -R matchguard:matchguard /app
USER matchguard

# Copy only the built executable jar from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose Spring Boot port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]