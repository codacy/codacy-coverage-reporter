#!/usr/bin/env bash

set -e +o pipefail

mkdir -p $HOME/.musl
cd $HOME/.musl

if [ "$ARCH" == "arm" ];
then
  echo "https://more.musl.cc/i686-linux-musl/aarch64-linux-musl-cross.tgz -output musl.tgz"
  curl https://more.musl.cc/i686-linux-musl/aarch64-linux-musl-cross.tgz --output musl.tgz
  tar -xf musl.tgz
  TOOLCHAIN_DIR=$HOME/.musl/aarch64-linux-musl-cross
else
  echo "http://more.musl.cc/10/x86_64-linux-musl/x86_64-linux-musl-native.tgz --output musl.tgz"
  curl http://more.musl.cc/10/x86_64-linux-musl/x86_64-linux-musl-native.tgz --output musl.tgz
  tar -xf musl.tgz
  TOOLCHAIN_DIR=$HOME/.musl/x86_64-linux-musl-native
fi

export CC=$TOOLCHAIN_DIR/bin/gcc

zlib='zlib-1.2.13'
zlibtargz=$zlib.tar.gz
echo "curl https://zlib.net/fossils/\$zlibtargz --output \$zlibtargz"
curl https://zlib.net/fossils/$zlibtargz --output $zlibtargz
tar -xf $zlibtargz

(
  cd $zlib
  if [ "$ARCH" == "arm" ];
  then
    ./configure --prefix=$TOOLCHAIN_DIR --static --build x86_64-pc-linux-gnu --host aarch64-linux-gnu
  else
    ./configure --prefix=$TOOLCHAIN_DIR --static
  fi
  make -j "$(nproc)"
  sudo make install
)

echo "$TOOLCHAIN_DIR/bin"