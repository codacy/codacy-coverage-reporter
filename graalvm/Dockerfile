FROM ghcr.io/graalvm/graalvm-ce:21.0.0

RUN gu install native-image

WORKDIR /opt/graalvm

COPY build-deps.sh .

RUN ./build-deps.sh

ENTRYPOINT [ "native-image" ]
