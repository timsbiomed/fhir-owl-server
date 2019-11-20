#!/bin/bash

# This is the Docker entrypoint script specified in
# Dockerfile2.   It supports invoking the container
# with both optional JVM runtime options as well as
# optional module arguments.
#
# Example:
#
#  'docker run -d -e JAVA_OPTS="-Xmx256M" fhir-owl-servre'
#

set -e

HOST_DOMAIN="host.docker.internal"
ping -q -c1 $HOST_DOMAIN > /dev/null 2>&1
if [ $? -ne 0 ]; then
  HOST_IP=$(ip route | awk 'NR==1 {print $3}')
  echo -e "$HOST_IP\t$HOST_DOMAIN" >> /etc/hosts
fi

if [ -n "$JAVA_OPTS" ]; then
  exec java "$JAVA_OPTS" -jar "${APP_HOME}"/webapp-runner.jar --port "$APP_PORT" "${APP_HOME}"/fhir-owl-server.war
else
   exec java -jar "${APP_HOME}"/webapp-runner.jar --port "$APP_PORT" "${APP_HOME}"/fhir-owl-server.war
fi
