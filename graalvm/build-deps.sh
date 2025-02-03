#!/usr/bin/env bash

set -e +o pipefail

mkdir -p $HOME/.musl
cd $HOME/.musl
uname -m
uname

if [ "$ARCH" == "arm" ];
then
  echo "https://more.musl.cc/i686-linux-musl/aarch64-linux-musl-cross.tgz -output musl.tgz"
  curl https://musl.cc/arm-linux-musleabi-cross.tgz  --output musl.tgz
  tar -xf musl.tgz
  TOOLCHAIN_DIR=$HOME/.musl/arm-linux-musleabi-cross
  mv $TOOLCHAIN_DIR/bin/arm-linux-musleabi-gcc $TOOLCHAIN_DIR/bin/gcc
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
  $TOOLCHAIN_DIR/bin/gcc --version
  cd $zlib
  if [ "$ARCH" == "arm" ];
  then
    CHOST=arm ./configure --prefix=$TOOLCHAIN_DIR --static
  else
    ./configure --prefix=$TOOLCHAIN_DIR --static
  fi
  make -j "$(nproc)"
  sudo make install
)

echo "$TOOLCHAIN_DIR/bin"