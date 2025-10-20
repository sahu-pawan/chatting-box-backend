# Multi-stage Dockerfile for Spring Boot app
# Build stage
FROM maven:3.9.4-amazoncorretto-17 AS build
WORKDIR /workspace
COPY pom.xml .
# Cache dependencies
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B -DskipTests package

# Run stage
FROM eclipse-temurin:17-jre-alpine

# metadata
LABEL org.opencontainers.image.title="chatting-box-backend"
LABEL org.opencontainers.image.description="ChattingBox Spring Boot backend"
LABEL org.opencontainers.image.licenses="MIT"

VOLUME /tmp

# copy the built jar from the build stage (match any jar produced)
COPY --from=build /workspace/target/*.jar /app/app.jar

# allow passing JVM options via environment variable
ENV JAVA_OPTS="-Xms128m -Xmx512m"

# create non-root user and ensure /tmp is writable
RUN addgroup -S appgroup && adduser -S appuser -G appgroup \
		&& mkdir -p /app && chown -R appuser:appgroup /app /tmp

# install curl for healthcheck (small footprint)
RUN apk add --no-cache curl

USER appuser
WORKDIR /app
EXPOSE 8080

# simple healthcheck (expects an actuator or root response). Adjust path if needed.
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
	CMD curl -fsS http://localhost:8080/actuator/health || curl -fsS http://localhost:8080/ || exit 1

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
