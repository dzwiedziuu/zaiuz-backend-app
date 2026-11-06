# --- Etap 1: budowanie pliku JAR ---------------------------------------------------------
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Najpierw same zależności – ta warstwa jest odtwarzana tylko po zmianie pom.xml.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
# Testy pomijane: test kontekstu wymaga bazy danych, której nie ma podczas budowania obrazu.
RUN mvn -B -q package -DskipTests

# --- Etap 2: obraz uruchomieniowy (sam JRE) ----------------------------------------------
FROM eclipse-temurin:21-jre
WORKDIR /app

RUN groupadd --system app && useradd --system --gid app app
COPY --from=build /app/target/zaiuz-backend-app-*.jar app.jar
USER app

# Aplikacja nasłuchuje na 8080; na Render w panelu ustawiona jest zmienna PORT=8080.
EXPOSE 8080
# Limit pamięci JVM względem limitu kontenera (darmowy plan Render: 512 MB).
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"
ENTRYPOINT ["java", "-jar", "app.jar"]
