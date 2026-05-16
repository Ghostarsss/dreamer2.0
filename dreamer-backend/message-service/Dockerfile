FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/message-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8095

ENTRYPOINT ["java","-jar","app.jar"]