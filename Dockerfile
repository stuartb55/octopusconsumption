FROM eclipse-temurin:19
RUN mkdir -p /app/
ADD build/libs/OctopusConsumption-0.0.1-SNAPSHOT.jar /app/OctopusConsumption-0.0.1.jar
ENTRYPOINT ["java", "-jar", "/app/OctopusConsumption-0.0.1.jar"]
