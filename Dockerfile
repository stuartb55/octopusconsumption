FROM amazoncorretto:19-alpine
RUN mkdir -p /app/
RUN ./gradlew build
ADD build/libs/OctopusConsumption-0.0.1-SNAPSHOT.jar /app/OctopusConsumption-0.0.1.jar
ENTRYPOINT ["java", "-jar", "/app/OctopusConsumption-0.0.1.jar"]