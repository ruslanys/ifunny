FROM openjdk:11-jre-slim
EXPOSE 8080

WORKDIR root/
ARG JAR_FILE=build/libs/ifunny-*.jar
ADD ${JAR_FILE} ./application.jar

ENTRYPOINT ["java", "-server", "-Xms2G", "-Xmx2G", "-XX:MaxMetaspaceSize=256M", "-XX:MaxDirectMemorySize=1G",\
            "-XX:+HeapDumpOnOutOfMemoryError", "-XX:HeapDumpPath=dump.hprof", "-Djava.security.egd=/dev/zrandom",\
            "-jar", "/root/application.jar"]
