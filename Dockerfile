FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Create the directories needed by the application
RUN mkdir -p /app/bookcontents /app/logs && \
    chmod 777 /app/bookcontents /app/logs

# Copy the pre-built jar from CI pipeline
COPY target/*.jar app.jar

# Environment variables
ENV JAVA_OPTS=""

# Health check
HEALTHCHECK --interval=30s --timeout=3s \
  CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

# Container configuration
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]