FROM clojure:temurin-23-lein-2.11.2-bookworm-slim AS build
COPY project.clj project.clj
RUN lein deps
COPY src src
COPY test test
COPY resources resourcesa
RUN lein compile
RUN lein with-profiles app uberjar
FROM openjdk:25-slim-bookworm
COPY --from=build /tmp/target/jepsen-xa-app-standalone.jar /jepsen-xa-app-standalone.jar
ENTRYPOINT ["java", "-jar", "/jepsen-xa-app-standalone.jar"]
