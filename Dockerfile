FROM oracle/graalvm-ce:19.3.0-java8

WORKDIR /opt/bin

RUN yum install -y wget && yum install -y xz && curl -s https://api.github.com/repos/upx/upx/releases/latest | grep "browser_download_url.*amd64_linux.tar.xz" | cut -d '"' -f 4 | wget -qi - -O upx-linux.tar.xz && tar xf upx-linux.tar.xz && ls && cp *amd64_linux/upx .
ENV PATH $PATH:/opt/bin

WORKDIR /opt/app
