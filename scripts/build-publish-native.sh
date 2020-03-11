#!/usr/bin/env bash

sbt clean
sbt 'show graalvm-native-image:packageBin'
docker build -t compressor:latest .
docker run --rm=true -it --user=root --entrypoint=bash -v $PWD/target/graalvm-native-image/:/opt/app/ compressor:latest -c "upx --lzma codacy-coverage-reporter -o codacy-coverage-reporter-compressed"
mv $PWD/target/graalvm-native-image/codacy-coverage-reporter-compressed codacy-coverage-reporter-linux-$(cat .version)
