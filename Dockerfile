# ── Etapa 1: Compilación ──────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
# Copiar solo el pom.xml primero para cachear dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -q
# Compilar el código fuente
COPY src ./src
RUN mvn clean package -DskipTests -q

# ── Etapa 2: Ejecución (imagen mínima) ────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
