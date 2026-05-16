FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/post-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8105

ENTRYPOINT ["java","-jar","app.jar"]