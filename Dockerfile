FROM oracle/graalvm-ce:20.2.0-java11

# Sourced from: https://docs.oracle.com/en/graalvm/enterprise/20/docs/reference-manual/native-image/StaticImages/

RUN \
    # musl
    curl -fsSL -o musl-1.2.1.tar.gz https://musl.libc.org/releases/musl-1.2.1.tar.gz && \
    tar -xvf musl-1.2.1.tar.gz && \
    cd musl-1.2.1 && \
    ./configure --disable-shared --prefix=/usr && \
    make && \
    make install && \
    rm -rf musl-1.2.1.tar.gz musl-1.2.1 && \
    # zlib
    curl -fsSL -o zlib-1.2.11.tar.gz https://zlib.net/zlib-1.2.11.tar.gz && \
    tar -xvf zlib-1.2.11.tar.gz && \
    cd zlib-1.2.11 && \
    export CC=musl-gcc && \
    ./configure --static --prefix=/usr && \
    make && \
    make install && \
    rm -rf zlib-1.2.11.tar.gz zlib-1.2.11 && \
    # libstdc++
    cp /usr/lib/gcc/x86_64-redhat-linux/4.8.2/libstdc++.a /usr/lib/ && \
    gu install native-image
