FROM openjdk:8-jdk-alpine

COPY --from=hengyunabc/arthas:latest /opt/arthas /opt/arthas

VOLUME /mnt/data
USER root

ENV JAVA_HEAP_OPTS="-Xms1g -Xmx2g"
ENV JAVA_OPTS=""

COPY target /opt/target
WORKDIR /opt/target
RUN find -type f -name "*.jar" | xargs -I{} mv {} app.jar
RUN mkdir -p logs/gc

CMD java $JAVA_HEAP_OPTS $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -XX:+UseConcMarkSweepGC -XX:CMSInitiatingOccupancyFraction=68 -jar app.jar