# Build the UDF JAR (no Striim SDK needed)
# For Open Processors, extract the SDK first — see README.md
FROM maven:3.9-eclipse-temurin-17
WORKDIR /app
COPY pom.xml .
COPY lib/ lib/
RUN mvn dependency:go-offline -B 2>/dev/null || true
COPY src/ src/
COPY config/ config/
RUN mvn package -B -q -DskipTests
