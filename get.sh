#!/usr/bin/env bash

set -e +o pipefail

# Log levels
i="\033[0;36m" # info
g="\033[0;32m" # green
e="\033[0;31m" # error
l="\033[0;90m" # log test
r="\033[0m" # reset


# Logger
# This function log messages
# Usage: log <LEVEL> <MESSAGE>
log() {
    echo -e " $1--> $l$2$r"
}


# Fatal logger
# This function log fatal messages
# Usage: fatal <MESSAGE> <EXIT_CODE>
fatal() {
    log "$e" "$1"
    exit "$([ $# -eq 2 ] && echo "$2" || echo 1)"
}


# Greetings
# This function print the greetings message for this coverage reporter
# Usage: greetings
greetings() {
cat << EOF
     ______          __
    / ____/___  ____/ /___ ________  __
   / /   / __ \/ __  / __ \`/ ___/ / / /
  / /___/ /_/ / /_/ / /_/ / /__/ /_/ /
  \____/\____/\__,_/\__,_/\___/\__, /
                              /____/

  Codacy Coverage Reporter

EOF
}

greetings

# Error trap
# This function prints succeeded or failed message depending on last exit status
# Usage: exit_trap
exit_trap() {
    EXIT_NUM=$?

    echo

    if [ $EXIT_NUM -eq 0 ];
    then
        log "$g" "Succeeded!"
    else
        fatal "Failed!"
    fi
}

trap exit_trap EXIT
trap 'fatal Interrupted' INT

# Temporary folder for downloaded files
if [ -z "$CODACY_REPORTER_TMP_FOLDER" ]; then
    if [ "$unamestr" = "Linux" ]; then
        CODACY_REPORTER_TMP_FOLDER="~/.cache/codacy"
    elif [ "$unamestr" = "Darwin" ]; then
        CODACY_REPORTER_TMP_FOLDER="~/Library/Caches/Codacy"
    else
        CODACY_REPORTER_TMP_FOLDER=".codacy-coverage"
fi
mkdir -p "$CODACY_REPORTER_TMP_FOLDER"

if [ -z "$CODACY_REPORTER_VERSION" ]; then
    CODACY_REPORTER_VERSION="latest"
fi

download() {
    local url="$1"
    local output="${2:--}"

    if [ -x "$(which curl)" ]; then
        curl -# -LS "$url" -o "$output"
    elif [ -x "$(which wget)" ] ; then
        wget "$url" -O "$output"
    else
        fatal "Could not find curl or wget, please install one."
    fi
}

get_version() {
    if [ "$CODACY_REPORTER_VERSION" == "latest" ]; then
        bintray_latest_api_url="https://api.bintray.com/packages/codacy/Binaries/codacy-coverage-reporter/versions/_latest"
        download $bintray_latest_api_url | sed -e 's/.*name[^0-9]*\([0-9]\{1,\}[.][0-9]\{1,\}[.][0-9]\{1,\}\).*/\1/'
    else
        echo "$CODACY_REPORTER_VERSION"
    fi
}

download_coverage_reporter() {
    local binary_name=$1
    local codacy_reporter=$2

    if [ ! -f "$codacy_reporter" ]
    then
        log "$i" "Download the codacy reporter $1... ($CODACY_REPORTER_VERSION)"

        bintray_api_url="https://dl.bintray.com/codacy/Binaries/$(get_version)/$binary_name"

        download "$bintray_api_url" "$codacy_reporter"
    else
        log "$i" "Using codacy reporter $1 from cache"
    fi
}

run() {
    eval "$@"
}

codacy_reporter_native_start_cmd() {
    local suffix=$1
    local codacy_reporter="$CODACY_REPORTER_TMP_FOLDER/codacy-coverage-reporter"
    download_coverage_reporter "codacy-coverage-reporter-$suffix" "$codacy_reporter"
    chmod +x $codacy_reporter
    run_command="$codacy_reporter"
}

codacy_reporter_jar_start_cmd() {
    local codacy_reporter="$CODACY_REPORTER_TMP_FOLDER/codacy-coverage-reporter-assembly.jar"
    download_coverage_reporter "codacy-coverage-reporter-assembly.jar" "$codacy_reporter"
    run_command="java -jar \"$codacy_reporter\""
}

run_command=""
unamestr=`uname`
if [ "$unamestr" = "Linux" ]; then
    codacy_reporter_native_start_cmd "linux"
elif [ "$unamestr" = "Darwin" ]; then
    codacy_reporter_native_start_cmd "darwin"
else
    codacy_reporter_jar_start_cmd
fi

if [ -z "$run_command" ]
then
    fatal "Codacy coverage reporter command could not be found."
fi

if [ "$#" -gt 0 ];
then
    run "$run_command $@"
else
    run "$run_command \"report\""
fi
