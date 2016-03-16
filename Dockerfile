FROM jboss/base-jdk:8

ADD target/ola.jar .

EXPOSE 8080

CMD java -jar ola.jar
