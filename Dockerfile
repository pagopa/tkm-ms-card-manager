FROM openjdk:8-jdk-alpine
VOLUME /tmp
COPY target/tkm-ms-card-manager-*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]