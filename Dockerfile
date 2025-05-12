# Étape 1 : Construction de l'application
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Étape 2 : Image finale pour l'exécution
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/smiback-0.0.1-SNAPSHOT.jar app.jar

# Volume pour données persistantes
VOLUME /var/data

# Port exposé
EXPOSE 8080

# Point d'entrée
ENTRYPOINT ["java", "-jar", "app.jar"]