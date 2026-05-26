# Bell Labs Spring official recommended image, can solve the operation problems of ARM chip domestic operating system
FROM bellsoft/liberica-openjdk-debian:21-cds

LABEL maintainer="openanik"
LABEL app-name="anik-ai-server"
LABEL description="Anik AI is an open-source enterprise AI Agent platform built on Java 25, Spring Boot 4, and Spring AI 2.0, providing a secure, controllable, and scalable distributed architecture for developing and operating intelligent agents."

ENV TZ=Asia/Shanghai LANG=C.UTF-8 LC_ALL=C.UTF-8

WORKDIR /anikai/server
ADD ./target/anik-ai-server-exec.jar app.jar

# Expose port number
EXPOSE 8080
# Data volume for mapping log directories
VOLUME /anikai/server/data/log
VOLUME /anikai/server/upload

# https://docs.docker.com/reference/build-checks/json-args-recommended/
CMD [ "java", "-Djava.security.egd=file:/dev/./urandom", "-XX:+HeapDumpOnOutOfMemoryError", "-XX:+UseZGC", "-jar", "app.jar" ]