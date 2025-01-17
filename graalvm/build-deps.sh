#!/usr/bin/env bash

set -e +o pipefail

mkdir -p $HOME/.musl
cd $HOME/.musl

if [$ARCH == "arm"];
then
  curl https://more.musl.cc/i686-linux-musl/arm-linux-musleabi-native.tgz -output musl.tgz
  TOOLCHAIN_DIR=$HOME/.musl/arm-linux-musleabi-native
else
  curl http://more.musl.cc/10/x86_64-linux-musl/x86_64-linux-musl-native.tgz --output musl.tgz
  TOOLCHAIN_DIR=$HOME/.musl/x86_64-linux-musl-native
fi
tar -xf musl.tgz

export CC=$TOOLCHAIN_DIR/bin/gcc

zlib='zlib-1.2.13'
zlibtargz=$zlib.tar.gz
curl https://zlib.net/fossils/$zlibtargz --output $zlibtargz
tar -xf $zlibtargz

(
  cd $zlib
  ./configure --prefix=$TOOLCHAIN_DIR --static
  make -j "$(nproc)"
  sudo make install
)

echo "$TOOLCHAIN_DIR/bin"
