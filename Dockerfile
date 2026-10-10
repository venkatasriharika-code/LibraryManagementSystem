FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM tomcat:9.0.100-jdk17-temurin
RUN rm -rf "$CATALINA_HOME/webapps/ROOT" "$CATALINA_HOME/webapps/ROOT.war"
COPY --from=build /app/target/LibraryManagementSystem.war "$CATALINA_HOME/webapps/ROOT.war"
EXPOSE 8080
CMD ["sh", "-c", "sed -i 's/port=\"8080\"/port=\"${PORT:-8080}\"/' \"$CATALINA_HOME/conf/server.xml\" && catalina.sh run"]