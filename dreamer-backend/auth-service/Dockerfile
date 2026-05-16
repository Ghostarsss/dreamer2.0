FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/auth-service-1.0-SNAPSHOT.jar app.jar

EXPOSE 8090

ENTRYPOINT ["java","-jar","app.jar"]