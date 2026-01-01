FROM eclipse-temurin:21

WORKDIR /app

COPY app-api/build/libs/app-api-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]