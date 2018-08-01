#!/usr/bin/env bash

set -e

current_directory="$( cd "$( dirname "$0" )" && pwd )"

${current_directory}/compile.sh
${current_directory}/test.sh
