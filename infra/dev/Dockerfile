FROM amazoncorretto:21
WORKDIR /app
COPY dojangkok-server-jar.jar dojangkok-server-jar.jar
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar /otel/opentelemetry-javaagent.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "dojangkok-server-jar.jar", "--spring.config.location=file:/app/config/application.yaml"]