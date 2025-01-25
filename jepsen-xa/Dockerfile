FROM clojure:temurin-23-lein-2.11.2-bookworm-slim AS build
COPY project.clj project.clj
RUN lein deps
COPY src src
COPY test test
COPY resources resourcesa
RUN lein compile
RUN lein with-profiles app uberjar
FROM openjdk:25-slim-bookworm
RUN apt-get update && \
	apt-get install -y sudo man-db curl iputils-ping logrotate tcpdump apt-transport-https libzip4 rsyslog psmisc ntpdate faketime netcat-openbsd vim unzip iproute2 bzip2 wget iptables dirmngr && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*
COPY --from=build /tmp/target/jepsen-xa-app-standalone.jar /jepsen-xa-app-standalone.jar
ENTRYPOINT ["java", "-jar", "/jepsen-xa-app-standalone.jar"]
