#!/usr/bin/env bash

set -e +o pipefail

mkdir -p $HOME/.musl
cd $HOME/.musl
uname -m
uname

if [ "$ARCH" == "arm" ];
then
  mkdir -p $HOME/.gcc
  cd $HOME/.gcc
  curl https://ftp.gnu.org/gnu/gcc/gcc-11.2.0/gcc-11.2.0.tar.xz  --output gcc.tgz
  tar -xf gcc.tgz
  TOOLCHAIN_DIR=$HOME/.gcc/gcc-11.2.0
else
  echo "http://more.musl.cc/10/x86_64-linux-musl/x86_64-linux-musl-native.tgz --output musl.tgz"
  curl http://more.musl.cc/10/x86_64-linux-musl/x86_64-linux-musl-native.tgz --output musl.tgz
  tar -xf musl.tgz
  TOOLCHAIN_DIR=$HOME/.musl/x86_64-linux-musl-native
  export CC=$TOOLCHAIN_DIR/bin/gcc
fi


zlib='zlib-1.2.13'
zlibtargz=$zlib.tar.gz
echo "curl https://zlib.net/fossils/\$zlibtargz --output \$zlibtargz"
curl https://zlib.net/fossils/$zlibtargz --output $zlibtargz
tar -xf $zlibtargz

(
  cd $zlib
  if [ "$ARCH" == "arm" ];
  then
    ./configure --help
    ./configure --target=aarch64-linux-gnu --prefix=$TOOLCHAIN_DIR
  else
    $CC --version
    ./configure --prefix=$TOOLCHAIN_DIR --static
  fi
  make -j "$(nproc)"
  sudo make install
)

echo "$TOOLCHAIN_DIR/bin"