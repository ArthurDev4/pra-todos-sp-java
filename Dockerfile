# Pra Todos SP - imagem para o Render (Render nao tem runtime Java nativo, por isso Docker)
# ---------- etapa 1: compilar ----------
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw -B -q dependency:go-offline
COPY src/ src/
RUN ./mvnw -B -q package -DskipTests

# ---------- etapa 2: executar ----------
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN useradd --system --create-home appuser
COPY --from=build /app/target/pratodossp.jar app.jar
USER appuser
# O Render define a variavel PORT; a aplicacao le server.port=${PORT:8080}
EXPOSE 8080
# Limites para caber nos 512 MB do plano gratuito do Render (medido: ~300 MB em uso)
ENV JAVA_OPTS="-Xmx256m -Xss512k -XX:MaxMetaspaceSize=160m -XX:ReservedCodeCacheSize=64m -XX:+UseSerialGC -XX:TieredStopAtLevel=1"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
