FROM fabric8/java-jboss-openjdk8-jdk:1.1.7

ENV JAVA_APP_JAR ola.jar
ENV AB_ENABLED jolokia
ENV AB_JOLOKIA_AUTH_OPENSHIFT true
ENV JAVA_OPTIONS -Xmx256m -Djava.security.egd=file:///dev/./urandom

EXPOSE 8080

ADD target/ola.jar /app/
