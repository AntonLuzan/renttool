FROM eclipse-temurin:17-jdk
LABEL authors="anton"
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew bootJar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "build/libs/renttool.jar"]