FROM eclipse-temurin:17-jdk-focal

WORKDIR /app

COPY app.jar app.jar

RUN mkdir -p /app/bookcontents /app/logs && \
    chmod 777 /app/bookcontents /app/logs

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]
