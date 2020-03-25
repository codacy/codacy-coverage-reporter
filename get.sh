#!/usr/bin/env bash

set -e +o pipefail

# Log levels
i="\033[0;36m" # info
g="\033[0;32m" # green
e="\033[0;31m" # error
l="\033[0;90m" # log test
r="\033[0m" # reset

# Temporary folder for downloaded files
codacy_temp_folder=".codacy-coverage"

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

mkdir -p "$codacy_temp_folder"

if [ -z "$CODACY_REPORTER_VERSION" ]; then
    CODACY_REPORTER_VERSION="latest"
fi

download_url() {
    grep browser_download_url | grep $1 | cut -d '"' -f 4
}

download_using_wget_or_curl() {
    api_url="https://api.github.com/repos/codacy/codacy-coverage-reporter/releases/$CODACY_REPORTER_VERSION"
    if [ -x "$(which curl)" ]; then
        curl -# -LS -o "$codacy_reporter" "$(curl -LSs $api_url | download_url $1)"
    elif [ -x "$(which wget)" ] ; then
        wget -O "$codacy_reporter" "$(wget -O - $api_url | download_url $1)"
    else
        fatal "Could not find curl or wget, please install one."
    fi
}

download_coverage_reporter() {
    if [ ! -f "$codacy_reporter" ]
    then
        log "$i" "Download the codacy reporter $1... ($CODACY_REPORTER_VERSION)"
        download_using_wget_or_curl $1
    else
        log "$i" "Using codacy reporter $1 from cache"
    fi
}

run() {
    eval "$@"
}

# Native executable binary
# This function returns the name of the binary
# Usage: codacy_reporter_native_start_cmd <OS-NAME>
codacy_reporter_native_start_cmd() {
    codacy_reporter="$codacy_temp_folder/codacy-coverage-reporter"    
    download_coverage_reporter $1
    chmod +x $codacy_reporter
    run_command="$codacy_reporter"
}

codacy_reporter_jar_start_cmd() {
    codacy_reporter="$codacy_temp_folder/codacy-coverage-reporter-assembly.jar"
    download_coverage_reporter "jar"
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
