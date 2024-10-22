FROM gradle:8.1.1-jdk17-focal AS builder

WORKDIR /build

# Only copy dependency-related files
COPY build.gradle.kts gradle.properties /build/

# Only download dependencies
# Eat the expected build failure since no source code has been copied yet
RUN gradle clean build --no-daemon > /dev/null 2>&1 || true

COPY ./ ./
RUN gradle clean assemble --no-daemon

FROM openjdk:17-slim AS runner
COPY --from=builder /build/build/libs/efex-backend-challenge-*-all.jar /app/app.jar

ARG APP_VERSION
ENV APP_VERSION=$APP_VERSION

EXPOSE 8080

CMD ["java", "-jar", "/app/app.jar"]
