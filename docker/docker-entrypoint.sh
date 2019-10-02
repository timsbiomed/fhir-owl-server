#!/bin/bash

# This is the Docker entrypoint script specified in
# Dockerfile.   It supports invoking the container
# with both optional JVM runtime options as well as
# optional module arguments.
#
# Example:
#
#  'docker run -d -e JAVA_OPTS="-Xmx256M" fhir-owl-servre'
#

set -e

if [ -n "$JAVA_OPTS" ]; then
  exec java "$JAVA_OPTS" -jar ${APP_HOME}/webapp-runner.jar --port $APP_PORT ${APP_HOME}/fhir-owl-server.war
else
   exec java -jar ${APP_HOME}/webapp-runner.jar --port $APP_PORT ${APP_HOME}/fhir-owl-server.war
fi
