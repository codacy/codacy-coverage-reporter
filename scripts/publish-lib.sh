#!/usr/bin/env bash

set -e

CURRENT_BRANCH="$(git symbolic-ref --short HEAD)"
PUBLISH_BRANCH="master"

if [ -n "$1" ]; then
  VERSION="$1"
else
  VERSION="1.0.0-${CURRENT_BRANCH}-SNAPSHOT"
fi

echo "Publishing version ${VERSION}"
if [[ -n "$CI" ]] && [[ "$CURRENT_BRANCH" == "$PUBLISH_BRANCH" || "$CIRCLE_BRANCH" == "$PUBLISH_BRANCH" ]]; then
  sbt 'set version := "'"${VERSION}"'"' 'set pgpPassphrase := Some("'"$SONATYPE_GPG_PASSPHRASE"'".toCharArray)' publishSigned
  sbt 'set version := "'"${VERSION}"'"' sonatypeRelease
else
  sbt 'set version := "'"${VERSION}"'"' publishLocal
fi
