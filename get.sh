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

download() {
    local url="$1"
    local output="${2:--}"

    if command -v curl > /dev/null 2>&1; then
        curl -# -LS "$url" -o "$output"
    elif command -v wget > /dev/null 2>&1; then
        wget "$url" -O "$output"
    else
        fatal "Could not find curl or wget, please install one."
    fi
}

download_reporter() {
    if [ "$os_name" = "Linux" ] || [ "$os_name" = "Darwin" ]; then
        # OS name lower case
        suffix=$(echo "$os_name" | tr '[:upper:]' '[:lower:]')
    else
        suffix="assembly.jar"
    fi
    local binary_name="codacy-coverage-reporter-$suffix"
    local reporter_path=$1

    if [ ! -f "$reporter_path" ]
    then
        log "$i" "Downloading the codacy reporter $binary_name... ($CODACY_REPORTER_VERSION)"

        binary_url="https://artifacts.codacy.com/bin/codacy-coverage-reporter/$CODACY_REPORTER_VERSION/$binary_name"

        download "$binary_url" "$reporter_path"
    else
        log "$i" "Codacy reporter $binary_name already in cache"
    fi
}

os_name=$(uname)

# Find the latest version in case is not specified
if [ -z "$CODACY_REPORTER_VERSION" ] || [ "$CODACY_REPORTER_VERSION" = "latest" ]; then
    CODACY_REPORTER_VERSION=$(download "https://artifacts.codacy.com/bin/codacy-coverage-reporter/latest")
fi

# Temporary folder for downloaded files
if [ -z "$CODACY_REPORTER_TMP_FOLDER" ]; then
    if [ "$os_name" = "Linux" ]; then
        CODACY_REPORTER_TMP_FOLDER="$HOME/.cache/codacy/coverage-reporter"
    elif [ "$os_name" = "Darwin" ]; then
        CODACY_REPORTER_TMP_FOLDER="$HOME/Library/Caches/Codacy/coverage-reporter"
    else
        CODACY_REPORTER_TMP_FOLDER=".codacy-coverage"
    fi
fi

# Set binary name
if [ "$os_name" = "Linux" ] || [ "$os_name" = "Darwin" ]; then
    reporter_filename="codacy-coverage-reporter"
else
    reporter_filename="codacy-coverage-reporter-assembly.jar"
fi

# Folder containing the binary
reporter_folder="$CODACY_REPORTER_TMP_FOLDER"/"$CODACY_REPORTER_VERSION"

# Create the reporter folder if not exists
mkdir -p "$reporter_folder"

# Set binary path
reporter_path="$reporter_folder"/"$reporter_filename"

download_reporter "$reporter_path"

if [ "$os_name" = "Linux" ] || [ "$os_name" = "Darwin" ]; then
    chmod +x "$reporter_path"
    run_command="$reporter_path"
else
    run_command="java -jar \"$reporter_path\""
fi

if [ -z "$run_command" ]
then
    fatal "Codacy coverage reporter binary could not be found."
fi

if [ "$#" -eq 1 ] && [ "$1" = "download" ];
then
    log "$g" "Codacy reporter download succeded";
elif [ "$#" -gt 0 ];
then
    eval "$run_command $*"
else
    eval "$run_command \"report\""
fi
