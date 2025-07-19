FROM openjdk:17-jdk-slim

RUN apt-get update && apt-get install -y postgresql-client && rm -rf /var/lib/apt/lists/*
WORKDIR /app

VOLUME /tmp
COPY build/libs/motel-management-apis-0.0.1-SNAPSHOT.jar app.jar
COPY wait-for-postgres.sh .
RUN chmod +x wait-for-postgres.sh
ENTRYPOINT ["./wait-for-postgres.sh"]

