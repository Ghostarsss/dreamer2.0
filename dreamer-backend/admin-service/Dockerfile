FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/admin-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8110

ENTRYPOINT ["java","-jar","app.jar"]