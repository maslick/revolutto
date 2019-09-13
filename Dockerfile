FROM maslick/minimalka:jdk11
WORKDIR /app
EXPOSE 8080
COPY build/libs/revolutto-0.1.jar ./app.jar
CMD java $JAVA_OPTIONS -jar app.jar