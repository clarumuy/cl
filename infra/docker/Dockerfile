# Compila el WAR en Docker; no hace falta Maven en el servidor.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml ./
COPY server/pom.xml server/pom.xml
COPY agent/pom.xml agent/pom.xml
RUN mvn -B -ntp -pl server -am dependency:go-offline
COPY server/src server/src
RUN mvn -B -ntp -pl server -am package -DskipTests

FROM tomcat:10.1-jre17-temurin
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /build/server/target/ROOT.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
