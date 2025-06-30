FROM openjdk:17-jdk-slim
WORKDIR /app

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B
COPY src ./src
COPY uploads/ /app/uploads/
RUN ./mvnw clean package -DskipTests


EXPOSE 8080
CMD ["java", "-jar", "target/AvanceHeladeria-0.0.1-SNAPSHOT.jar"]