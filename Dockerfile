FROM maven:3.9.5-eclipse-temurin-17
WORKDIR /app
COPY pom.xml /app
COPY src /app/src
RUN mvn dependency:go-offline -B
CMD ["sh", "-c", "mvn test"]
