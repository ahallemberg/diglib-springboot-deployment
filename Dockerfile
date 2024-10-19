FROM eclipse-temurin:17-jdk-focal

WORKDIR /app

# Copy the jar file
COPY *.jar app.jar

# Create directory for book contents with proper permissions
RUN mkdir -p /app/bookcontents && \
    chmod 777 /app/bookcontents

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]