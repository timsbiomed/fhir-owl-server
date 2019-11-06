# fhir owl

## Getting Started

To build and install, run 

```
mvn clean install 
docker-compose -f docker/docker-compose.yml up
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





