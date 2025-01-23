#!/usr/bin/env bash

set -e +o pipefail

mkdir -p $HOME/.musl
cd $HOME/.musl

if [ "$ARCH" == "arm" ];
then
  echo "https://more.musl.cc/i686-linux-musl/arm-linux-musleabi-native.tgz -output musl.tgz"
  curl https://more.musl.cc/i686-linux-musl/arm-linux-musleabi-native.tgz -output musl.tgz
  TOOLCHAIN_DIR=$HOME/.musl/arm-linux-musleabi-native
else
  echo "http://more.musl.cc/10/x86_64-linux-musl/x86_64-linux-musl-native.tgz --output musl.tgz"
  curl http://more.musl.cc/10/x86_64-linux-musl/x86_64-linux-musl-native.tgz --output musl.tgz
  TOOLCHAIN_DIR=$HOME/.musl/x86_64-linux-musl-native
fi
