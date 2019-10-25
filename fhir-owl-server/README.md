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