FROM openjdk:8-jre

ENV WAR_FILE fhir-owl-server-1.0-SNAPSHOT.war
ENV APP_HOME /app
ENV APP_PORT 8080

COPY ./fhir-owl-server/target/$WAR_FILE $APP_HOME/fhir-owl-server.war
COPY ./fhir-owl-server/target/dependency/webapp-runner.jar $APP_HOME
COPY ./docker/fhir-owl-server/docker-entrypoint.sh $APP_HOME

# Create user/group
RUN groupadd fhir && \
    useradd -r -d $APP_HOME -g fhir -M fhirowl && \
    chown -R fhirowl.fhir $APP_HOME && \
    chown -R fhirowl.fhir ${APP_HOME}/docker-entrypoint.sh && \
    chmod +x ${APP_HOME}/docker-entrypoint.sh

USER fhirowl

WORKDIR $APP_HOME
EXPOSE $APP_PORT
ENTRYPOINT ["./docker-entrypoint.sh"]
