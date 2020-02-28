#!/usr/bin/env bash

#
# Simple Wrapper to build a native binary for your sbt application.
#
# Configuration:
# * Target:
#   * `native` - Builds the binary for the machine arch (Requires GraalVM)
#   * `docker` - Builds the binary for linux using a docker (Requires Docker)
#
# Example:
# ./scripts/publish-native.sh -n codacy-coverage-reporter -m com.codacy.CodacyCoverageReporter -t docker 1.0.0
#

set -e

VERSION="1.0.0-$(git symbolic-ref --short HEAD)-SNAPSHOT"
TARGET="native"
OS_TARGET="$(uname | awk '{print tolower($0)}')"

function usage() {
  echo >&2 "Usage: $0 -n <app-name> -m <main-class> [-t target (native)] [app-version (1.0.0-<branch-name>-SNAPSHOT)]"
}

while getopts :t:n:m:h opt
do
  case "$opt" in
    t)
      TARGET="$OPTARG"

      if [[ "${TARGET}" == "docker" && "${OS_TARGET}" == "darwin" ]]
      then
        echo >&2 "Target docker can only build binaries for linux."
        OS_TARGET="linux"
      fi
      ;;
    n)
      APP_NAME="$OPTARG"
      ;;
    m)
      APP_MAIN_CLASS="$OPTARG"
      ;;
    h | ?)
      usage
      exit 0
      ;;
    *)
      usage
      exit 1
      ;;
	esac
done

shift $((OPTIND-1))

if [ -z "$APP_NAME" ]; then
  echo >&2 "App name was not provided."
  usage
  exit 1
fi

if [ -z "$APP_MAIN_CLASS" ]; then
  echo >&2 "Main class was not provided."
  usage
  exit 1
fi

if [ -n "$1" ]; then
  VERSION="$1"
fi

function app_classpath() {
  echo $(cat /dev/null | sbt ';compile;export runtime:fullClasspath' | tail -n 1)
}

function build_cmd() {
  local BINARY_NAME=$1
  local APP_MAIN_CLASS=$2
  local APP_CLASSPATH=$3
  local FLAGS='-O1'
  FLAGS+=' --enable-http --enable-https --enable-url-protocols=http,https,file,jar --enable-all-security-services'
  FLAGS+=' -H:+JNI -H:IncludeResourceBundles=com.sun.org.apache.xerces.internal.impl.msg.XMLMessages'
  FLAGS+=' -H:+ReportExceptionStackTraces'
  FLAGS+=' --no-fallback --initialize-at-build-time'
  FLAGS+=' --report-unsupported-elements-at-runtime'

  if [ "${OS_TARGET}" != "darwin" ]
  then
    FLAGS+=' --static'
  fi

  echo 'native-image -cp '"${APP_CLASSPATH}"' '"${FLAGS}"' -H:Name='"${BINARY_NAME}"' -H:Class='"${APP_MAIN_CLASS}"
}

echo "Publishing ${APP_NAME} binary version ${VERSION} for ${OS_TARGET}"
BINARY_NAME="${APP_NAME}-${OS_TARGET}-${VERSION}"

BUILD_CMD+="cd "$PWD" && $(build_cmd ${BINARY_NAME} ${APP_MAIN_CLASS} "$(app_classpath)")"

echo "Going to run ${BUILD_CMD}"

INSTALL_UPX="cd $PWD && yum install -y wget && yum install -y xz && curl -s https://api.github.com/repos/upx/upx/releases/latest | grep \"browser_download_url.*amd64_linux.tar.xz\" | cut -d '\"' -f 4 | wget -qi - -O upx-linux.tar.xz && tar xf upx-linux.tar.xz && ls && cp *amd64_linux/upx ."
COMPRESS_USING_UPX="./upx ${BINARY_NAME} && rm -rf upx*"

case "$TARGET" in
  native)
    ${BUILD_CMD}
    ;;
  docker)
    docker run \
      --rm=true \
      -it \
      --user=root \
      --entrypoint=bash \
      -v $HOME:$HOME:ro \
      -v $PWD:$PWD \
      oracle/graalvm-ce:19.3.0-java8 \
      -c "yum install -y libstdc++-static && ${INSTALL_UPX} && gu install native-image && ${BUILD_CMD} && ${COMPRESS_USING_UPX}"
    ;;
  *)
    echo >&2 "Could not find command for target $TARGET"
    exit 1
    ;;
esac
