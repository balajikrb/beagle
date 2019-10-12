FROM openjdk:8-jre-alpine
VOLUME /tmp
RUN apk add --no-cache curl
ARG JAR_FILE
ADD ${JAR_FILE} app.jar
HEALTHCHECK --interval=10s --timeout=3s CMD curl --fail --silent --head http://localhost:8080 | grep "HTTP/1.1 200" || exit 1
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=1044", "-jar", "/app.jar"]
