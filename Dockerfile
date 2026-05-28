# === 构建阶段 ===
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml ./
RUN mvn dependency:resolve dependency:resolve-plugins -B -q
COPY src src
RUN mvn package -DskipTests -B -q

# === 运行阶段 ===
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S tingcheng && adduser -S tingcheng -G tingcheng
COPY --from=builder /app/target/*.jar app.jar
RUN mkdir -p /app/data/uploads && chown -R tingcheng:tingcheng /app
VOLUME /app/data/uploads
USER tingcheng
EXPOSE 8092
ENV SPRING_PROFILES_ACTIVE=dev \
    SERVER_PORT=8092
ENTRYPOINT ["java", "-jar", "app.jar"]
