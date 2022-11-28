#!/usr/bin/env bash

set -e +o pipefail

export TOOLCHAIN_DIR=$HOME/.musl
mkdir -p $TOOLCHAIN_DIR
cd $TOOLCHAIN_DIR

curl http://more.musl.cc/10/x86_64-linux-musl/x86_64-linux-musl-native.tgz --output musl.tgz
tar -xf musl.tgz
export CC=$TOOLCHAIN_DIR/bin/gcc

zlib='zlib-1.2.13'
zlibtargz=$zlib.tar.gz
curl https://zlib.net/$zlibtargz --output $zlibtargz
tar -xf $zlibtargz

(
  cd $zlib
  ./configure --prefix=$TOOLCHAIN_DIR --static
  make -j "$(nproc)"
  sudo make install
)
