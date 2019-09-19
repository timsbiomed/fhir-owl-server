# FHIR OWL Server

This is a demonstration of delivering OWL ontologies on a FHIR terminology server. 

It is built as a RESTful server on HAPI-FHIR.

## Getting started

To run the server, use

```
mvn clean jetty:run  
``` 

Visit http://localhost:8080/fhirowl/metadata or http://localhost:8080/fhirowl/CodeSystem/1

To run integration test (Start the service and test the API), 

```
mvn verify -Pintegration 
```