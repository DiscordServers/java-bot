FROM gradle:jdk8-alpine as builder
USER root

ENV JAVA_OPTS="-Xmx12g"

WORKDIR /code

ADD build.gradle settings.gradle config.json /code/
RUN gradle build

ADD src /code/src
RUN gradle shadowJar \
    && mv /code/build/libs/bot-0.1.0-all.jar /code/bot.jar \
    && rm -r /code/src /code/build *.gradle

FROM openjdk:8-jre-alpine

COPY --from=builder /code/bot.jar .

CMD ["java", "-jar", "bot.jar"]