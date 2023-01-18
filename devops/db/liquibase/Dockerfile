FROM postgres:alpine

USER root

RUN apk --no-cache add curl
RUN apk --no-cache add dos2unix
RUN apk --no-cache add openjdk8
RUN apk --no-cache add which

RUN curl -LO https://github.com/liquibase/liquibase/releases/download/v4.8.0/liquibase-4.8.0.tar.gz \
	&& mkdir -p liquibase \
	&& tar xf liquibase-4.8.0.tar.gz -C liquibase \
	&& chmod -R a+rwx ./liquibase/ \
	&& ln -sf ./liquibase /usr/local/bin

ADD lib/postgresql-42.5.0.jar ./liquibase/
ADD run_image.sh ./
ADD changelogs/ ./changelogs

RUN chmod -R a+rwx run_image.sh \
	&& dos2unix run_image.sh

CMD ./run_image.sh