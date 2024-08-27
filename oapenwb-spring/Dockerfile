# syntax=docker/dockerfile:1

##
# Dockerfile for service oapenwb-spring.
##

FROM eclipse-temurin:22.0.1_8-jre-alpine
LABEL org.opencontainers.image.authors="ule.dk"

ARG APP_VERSION=0.1.0-SNAPSHOT

# HTTP port - for public API
EXPOSE 8080
# gRPC port - only for internal communication(!)
EXPOSE 9090

# TODO Configure logging to log to host directory here
RUN mkdir /logs
WORKDIR /app

COPY target/oapenwb-spring-${APP_VERSION}.jar oapenwb-spring.jar

# "-Dspring.profiles.active=docker",
ENTRYPOINT [ "java", "-jar", "oapenwb-spring.jar" ]
