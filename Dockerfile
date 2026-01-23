# Etapa 1: Construcción
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app
COPY . .
# Damos permisos de ejecución al wrapper de Maven que ya tienes
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# Etapa 2: Ejecución
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
# Copiamos el jar generado (revisa que el nombre coincida con tu pom.xml)
COPY --from=build /app/target/nexus-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]s