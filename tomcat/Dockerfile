# https://hub.docker.com/_/tomcat/
FROM tomcat:8
RUN apt-get update && apt-get install openjdk-8-jdk maven wget -y

COPY lib/ /usr/local/tomcat/lib/
COPY conf/ /usr/local/tomcat/conf/

COPY source/experiment-lazy-filtering/ /usr/local/src/experiment-lazy-filtering/
RUN mvn clean install -f /usr/local/src/experiment-lazy-filtering/
RUN cp /usr/local/src/experiment-lazy-filtering/target/experiment-lazy-filtering.war /usr/local/tomcat/webapps/

RUN wget https://github.com/glowroot/glowroot/releases/download/v0.13.1/glowroot-0.13.1-dist.zip -O /tmp/glowroot-0.13.1-dist.zip
RUN unzip /tmp/glowroot-0.13.1-dist.zip -d /tmp/
RUN mv /tmp/glowroot/ /opt/glowroot/
COPY glowroot/admin.json /opt/glowroot/
