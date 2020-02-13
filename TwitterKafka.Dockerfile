# This Dockerfile has two required ARGs to determine which base image
# to use for the JDK and which sbt version to install.

ARG OPENJDK_TAG=8u232
FROM openjdk:${OPENJDK_TAG}

ADD twitter-kafka-scala/twitter-kafka-scala/app-assembly.jar app-assembly.jar
ADD run-jar.sh run-jar.sh

RUN chmod 755 run-jar.sh && chmod 755 app-assembly.jar

ENTRYPOINT ["sh", "run-jar.sh"]