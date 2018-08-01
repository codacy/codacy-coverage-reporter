#!/usr/bin/env bash

set -e

if [ -n "$1" ]; then
    export CODACY_PROJECT_TOKEN="$1"
fi

sbt coverage test
sbt coverageReport
sbt coverageAggregate

if [ -z "$CODACY_PROJECT_TOKEN" ]; then
    echo "CODACY_PROJECT_TOKEN not found. Skipping send coverage to Codacy."
else
    sbt codacyCoverage
fi
