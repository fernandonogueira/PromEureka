FROM openjdk:8-jre-alpine

LABEL maintainer='PromEureka Contributors'

RUN apk add --update bash jq curl && rm -rf /var/cache/apk/*

ADD target/promeureka.jar app.jar

CMD ["java", "-jar", "app.jar"]