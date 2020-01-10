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
# Usage: fatal <LEVEL> <MESSAGE>
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

codacy_reporter="$codacy_temp_folder/codacy-coverage-reporter-assembly.jar"

if [ ! -f "$codacy_reporter" ]
then
    log "$i" "Download the codacy reporter... ($CODACY_REPORTER_VERSION)"
    curl -LS -o "$codacy_reporter" "$(curl -LSs https://api.github.com/repos/codacy/codacy-coverage-reporter/releases/$CODACY_REPORTER_VERSION | jq -r '.assets | map({name, browser_download_url} | select(.name | endswith(".jar"))) | .[0].browser_download_url')"
else
    log "$i" "Using codacy reporter from cache"
fi

if [ "$#" -gt 0 ];
then
    java -jar "$codacy_reporter" $@
else
    java -jar "$codacy_reporter" report
fi
