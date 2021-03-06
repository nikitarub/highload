FROM ubuntu:16.04

RUN apt-get -y  update

RUN apt-get install -y openjdk-8-jdk-headless tree

ENV WORK /hl
WORKDIR $WORK/

ADD . .
EXPOSE 80

CMD java -jar build/libs/highload-1.0.jar
