#!/usr/bin/env bash

set -e

sbt compile
sbt test:compile
