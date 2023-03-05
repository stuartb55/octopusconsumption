FROM amazoncorretto:19-alpine
RUN mkdir -p /app/
ADD /home/runner/work/octopusconsumption/octopusconsumption/build/libs/OctopusConsumption-0.0.1-SNAPSHOT.jar /app/OctopusConsumption-0.0.1.jar
ENTRYPOINT ["java", "-jar", "/app/OctopusConsumption-0.0.1.jar"]
