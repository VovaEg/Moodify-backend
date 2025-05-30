FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

ARG JAR_FILE=target/moodify-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]