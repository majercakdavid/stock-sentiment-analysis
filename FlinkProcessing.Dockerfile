ARG OPENJDK_TAG=8u232
FROM openjdk:${OPENJDK_TAG}

ADD flink-processing/flink-processing/app-assembly.jar app-assembly.jar
ADD run-jar.sh run-jar.sh

RUN chmod 755 run-jar.sh && chmod 755 app-assembly.jar

ENTRYPOINT ["sh", "run-jar.sh"]