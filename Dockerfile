# This Dockerfile has two required ARGs to determine which base image
# to use for the JDK and which sbt version to install.

ARG OPENJDK_TAG=8u232
FROM openjdk:${OPENJDK_TAG}

# ARG SBT_VERSION=1.3.7
# ARG PROJ_DIR=twitter-kafka-scala/twitter-kafka-scala

# # Install sbt
# RUN \
#   curl -L -o sbt-$SBT_VERSION.deb https://dl.bintray.com/sbt/debian/sbt-$SBT_VERSION.deb && \
#   dpkg -i sbt-$SBT_VERSION.deb && \
#   rm sbt-$SBT_VERSION.deb && \
#   apt-get update && \
#   apt-get install sbt && \
#   sbt sbtVersion

ADD ./twitter-kafka-scala/twitter-kafka-scala/app-assembly.jar ./app-assembly.jar

# VOLUME ["/twitter-kafka-scala/twitter-kafka-scala"]
# WORKDIR /twitter-kafka-scala/twitter-kafka-scala
# RUN sbt assembly

ADD ./run-jar.sh run-jar.sh

RUN chmod 755 run-jar.sh && chmod 755 app-assembly.jar

ENTRYPOINT ["sh", "run-jar.sh"]