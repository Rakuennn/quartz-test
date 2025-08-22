FROM maven:3.8.4-openjdk-17 AS builder

WORKDIR /opt/app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

FROM openjdk:17-jdk-slim

WORKDIR /opt/app

EXPOSE 8080
COPY --from=builder /opt/app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
