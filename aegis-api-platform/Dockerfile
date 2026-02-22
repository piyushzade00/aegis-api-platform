# -------- Stage 1: Build --------
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests


# -------- Stage 2: Runtime --------
FROM eclipse-temurin:17-jre-alpine

# Create non-root user
RUN addgroup -S aegisgroup && adduser -S aegisuser -G aegisgroup

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

# Change ownership
RUN chown -R aegisuser:aegisgroup /app

# Switch to non-root
USER aegisuser

EXPOSE 8080

ENTRYPOINT ["java","-XX:+UseContainerSupport","-XX:MaxRAMPercentage=75.0","-XX:+ExitOnOutOfMemoryError","-jar","app.jar"]