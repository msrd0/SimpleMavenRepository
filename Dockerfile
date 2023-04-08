FROM gradle:8-jdk17 AS builder
USER root

ARG project=SimpleMavenRepo
RUN mkdir -p /src/ /usr/local/${project}/

WORKDIR /src/
COPY . /src/

RUN gradle :distTar \
	&& tar xf build/distributions/${project}-*.tar \
    	-C /usr/local/${project}/ \
    	--strip-components=1 \
	&& rm /usr/local/${project}/bin/${project}.bat

########################

FROM alpine:3.17

VOLUME /data

ARG project=SimpleMavenRepo
RUN apk add --no-cache openjdk17-jre-headless
COPY --from=builder /usr/local/${project}/ /usr/local/${project}/

EXPOSE 8234

# we need that script since we can't access project in the container
RUN echo -e "#!/bin/ash\nset -e\n/usr/local/${project}/bin/${project}" >/init.sh \
    && chmod 555 /init.sh

WORKDIR /data
CMD ["/init.sh"]
