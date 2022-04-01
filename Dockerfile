FROM alpine:3.15.3

ARG nativeImageLocation=target/graalvm-native-image/codacy-coverage-reporter

COPY ${nativeImageLocation} /app/codacy-coverage-reporter

WORKDIR /code

ENTRYPOINT [ "/app/codacy-coverage-reporter" ]
