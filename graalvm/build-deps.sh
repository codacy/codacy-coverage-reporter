#!/usr/bin/env bash

set -e +o pipefail

musl='musl-1.2.2'
musltargz=$musl.tar.gz
curl https://musl.libc.org/releases/$musltargz --output $musltargz
tar -xf $musltargz
dest=/usr/local

(
  cd $musl
  ./configure --disable-shared --prefix=$dest
  make -j "$(nproc)"
  make install
)

zlib='zlib-1.2.11'
zlibtargz=$zlib.tar.gz
curl https://zlib.net/$zlibtargz --output $zlibtargz
tar -xf $zlibtargz

(
  cd $zlib
  export CC=musl-gcc
  ./configure --static --prefix=$dest
  make -j "$(nproc)"
  make install
)
