FROM alpine:3

RUN apk add --no-cache curl

COPY initialise.sh create-database.json create-instance.json /init/

WORKDIR /init
ENTRYPOINT [ "/bin/sh", "initialise.sh" ]
