FROM openjdk:8-jdk-alpine
VOLUME /tmp
COPY build/libs/*.jar app.jar
COPY src/main/resources/gtfs.zip /resources/gtfs.zip
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]