FROM rhel7

RUN yum -y install java && yum clean all

EXPOSE 8080

CMD java -jar ola.jar

ADD target/ola.jar .
