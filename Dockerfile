FROM amazoncorretto:17-alpine-jdk AS builder

WORKDIR /app

COPY . .

RUN apk add --no-cache maven && \
    mvn package -Dmaven.test.skip=true

FROM amazoncorretto:17-alpine-jdk

WORKDIR /app

COPY --from=builder /app/target/em-card-service-0.0.1-SNAPSHOT.jar .

EXPOSE 8100

CMD ["java", "-jar", "em-card-service-0.0.1-SNAPSHOT.jar"]