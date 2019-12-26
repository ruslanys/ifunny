FROM openjdk:11-jre-slim

WORKDIR root/

ADD build/libs/crawler-*.jar ./application.jar

EXPOSE 8080

CMD java -server -Xmx1024M -Djava.security.egd=/dev/zrandom -jar /root/application.jar
