# Glowroot APM, JSF Application and Stress-Test Simulation with docker-compose  
> Performance monitoring of a JSF webapp in a Tomcat applicationserver using the fantastic APM [glowroot](https://glowroot.org/)  
There is also a Stress-Test simulation for [Gatling](https://gatling.io)   
The environment is set up with Docker

### Start DockerEnv
```bash
docker-compose up --build
``` 
**URL to Tomcat Manager App**
 (username: tomcat password: tomcat)  
``http://localhost:8080/``  
**URL to JSF Application**  
``http://localhost:8080/experiment-lazy-filtering/``  
**URL to Glowroot**  
``http://localhost:4040/``


### Shoot it using [Gatling](https://gatling.io)

1. Download gatling https://gatling.io/download/
2. Start Gatling ``gatling-charts-highcharts-bundle-3.0.3/bin/gatling.sh -sf gatling-simulations  -s JSF_Simulation``

## Documentation
### docker-compose.yml
``` yml
version: '2'
services:
  mysql_db:
    build: mysql
    command: --init-file /tmp/sql/init.sql
    environment:
        MYSQL_ROOT_PASSWORD: secret
  tomcat:
    build: 'tomcat'
    ports:
        - 9010:9010
        - 8080:8080
        - 4040:4040
    environment:
        - "JAVA_OPTS=-javaagent:/opt/glowroot/glowroot.jar -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9010 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Xmx1024m -Xss4096k"
    depends_on:
      - mysql_db
```
***
### Manual Installation [Glowroot-Embedded](https://github.com/glowroot/glowroot/wiki/Agent-Installation-%28with-Embedded-Collector%29)   (already done when using docker-compose.yml)
**Dowsnload the java-agent**  
- Download the glowroot-${VERSION}-dist.zip [glowroot/releases](https://github.com/glowroot/glowroot/releases)

**Configure the java-agent**  
- unzip  
- add the **admin.json** file in the same directory as glowroot.jar to allow acces from any hosts (default host: localhost, default port: 4000)
```json
{
  "web": {
    "bindAddress": "0.0.0.0",
    "port": "4040"
  }
}
```

### set the JAVA_OPTS  
Add ``-javaagent:path/to/glowroot.jar`` to your application's JVM args.  
see here for details: [Where-are-my-application-server's-JVM-args](https://github.com/glowroot/glowroot/wiki/Where-are-my-application-server's-JVM-args%3F)

**Dockerfile**  
```dockerfile
FROM tomcat
ENV JAVA_OPTS="-javaagent:/opt/glowroot/glowroot.jar"
COPY lib/ /usr/local/tomcat/lib/
COPY conf/ /usr/local/tomcat/conf/

RUN wget https://github.com/glowroot/glowroot/releases/download/v0.13.1/glowroot-0.13.1-dist.zip -O /tmp/glowroot-0.13.1-dist.zip
RUN unzip /tmp/glowroot-0.13.1-dist.zip -d /tmp/
RUN mv /tmp/glowroot/ /opt/glowroot/
COPY glowroot/admin.json /opt/glowroot/
```