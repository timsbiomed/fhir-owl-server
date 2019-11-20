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

# exec neo4j start
exec neo4j console
