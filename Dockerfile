FROM openjdk:11-jre-slim

WORKDIR root/

ADD build/libs/ifunny-*.jar ./application.jar

EXPOSE 8080

CMD java -server -Xms2G -Xmx2G -XX:MaxMetaspaceSize=256M -XX:MaxDirectMemorySize=1G \
         -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=dump.hprof \
         -Djava.security.egd=/dev/zrandom -jar /root/application.jar
