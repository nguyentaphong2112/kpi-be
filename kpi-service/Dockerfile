FROM amazoncorretto:17-alpine

WORKDIR /app

COPY target/kpi-service-1.0-SNAPSHOT.jar app.jar

EXPOSE 8879

ENTRYPOINT ["java", "-jar", "app.jar"]