FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/document-management-0.0.1-SNAPSHOT.jar document-management.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $SPRING_BOOT_JAVA_OPTS -jar document-management.jar"]
