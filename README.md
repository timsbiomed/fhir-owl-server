# FHIR OWL Server

Not the same as [aehrc/fhir-owl](https://github.com/aehrc/fhir-owl). A proof of concept REST service for CDMH / CCDH.


## Getting Started

To build and standup the docker containers,  run

```
mvn clean install 
docker-compose -f docker/docker-compose.yml up
```

Load data into graphdb

```
curl -X POST http://localhost:7300/rest/repositories -H 'Content-Type: multipart/form-data' -F "config=@docker/graphdb/fhirowl-config.ttl"
curl -X POST http://localhost:7300/rest/data/import -H 'Content-Type: multipart/form-data' -F "config=@docker/graphdb/fhirowl-config.ttl" 
 
```

## Developer's Guide: 

To run targets in only one module, use `-pl` parameter. For example, the following will run the jetty:run task in 
the fhir-owl-server module. 
 
```
mvn jetty:run -pl fhir-owl-server 
```

To run the docker containers in the background, run 

```
docker-compose -f docker/docker-compose.yml up -d 
```

To build and publish docker image for fhir-owl-server

```
docker build -t jiaola/fhir-owl-server -f docker/fhir-owl-server/Dockerfile .
docker push jiaola/fhir-owl-server:latest 
```

To run the docker image with port forwarding: 

```
docker run -d --name fhir-owl -p 8094:8080 --restart unless-stopped jiaola/fhir-owl-server 
```





