FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY certs/* certs/
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]