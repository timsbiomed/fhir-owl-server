# FHIR OWL Server

This is a demonstration of delivering OWL ontologies on a FHIR terminology server. 

It is built as a RESTful server on HAPI-FHIR.

## Getting started

To run the server, use

```
mvn clean jetty:run  
``` 

Visit http://localhost:8080/metadata or http://localhost:8080/CodeSystem/1

To run integration test (Start the service and test the API), 

```
mvn verify -Pintegration 
```

## Build docker images

To build the docker image for fhir-owl-server

``` 
docker build -t jiaola/fhir-owl-server -f docker/fhir-owl-server/Dockerfile .
```

For graphdb-free, use the graphdb docker instructions to build a local image. For details, 
see https://github.com/dhlab-basel/docker-graphdb-free

Run docker compose to standup the app:

```
docker-compose -f docker/docker-compose-graphdb.yml up -d
```

Use REST API to create a repository in graphdb. 

``` 
curl -X POST\
    http://localhost:7300/rest/repositories\
    -H 'Content-Type: multipart/form-data'\
    -F "config=@docker/graphdb/fhirowl-config.ttl"
```

Create a ssh tunnel, and mannualy import the stato.owl file into the default graph. TODO: If we decide to 
continue to use graphdb, we should use named graphs, and use the REST api to import owl files, and integrate
the steps in docker. 