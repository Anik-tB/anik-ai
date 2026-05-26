FROM bellsoft/liberica-openjdk-debian:21-cds
LABEL maintainer="openanik"

ADD ./target/anik-ai-agent-example.jar anik-ai-agent-example.jar

# Expose port number
EXPOSE 8090

WORKDIR /

# Start on boot
ENTRYPOINT ["sh","-c","java -jar $JAVA_OPTS /anik-ai-agent-example.jar $PARAMS"]

