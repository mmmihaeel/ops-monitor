FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY .mvn .mvn
COPY mvnw mvnw
COPY pom.xml pom.xml
RUN chmod +x mvnw && ./mvnw -q -DskipTests dependency:go-offline

COPY src src
RUN ./mvnw -q -DskipTests package

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN useradd --system --uid 10001 --create-home spring
USER spring

COPY --from=build /workspace/target/ops-monitor-1.0.0-SNAPSHOT.jar /app/ops-monitor.jar

EXPOSE 3005
ENTRYPOINT ["java", "-jar", "/app/ops-monitor.jar"]
