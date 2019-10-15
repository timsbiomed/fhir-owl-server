# fhir owl

## Getting Started

To build and install, run 

```
mvn clean install 
```

To run targets in only one module, use `-pl` parameter. For example, the following will run the jetty:run task in 
the fhir-owl-server module. 
 
```
mvn jetty:run -pl fhir-owl-server 
```

To build and publish docker images,

```
docker build -t jiaola/fhir-owl-server . 
docker push jiaola/fhir-owl-server:latest 
```

To run the docker image with port forwarding: 

```
docker run -d --name fhir-owl -p 8094:8080 --restart unless-stopped jiaola/fhir-owl-server 
```