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

download_stdout() {
    local url="$1"

    if command -v curl > /dev/null 2>&1; then
        curl -# -LS "$url" -o-
    elif command -v wget > /dev/null 2>&1; then
        wget "$url" -O-
    else
        fatal "Could not find curl or wget, please install one."
    fi
}

download_file() {
    local url="$1"

    if command -v curl > /dev/null 2>&1; then
        curl -# -LS "$url" -O
    elif command -v wget > /dev/null 2>&1; then
        wget "$url"
    else
        fatal "Could not find curl or wget, please install one."
    fi
}

checksum() {
  local file_name="$1"
  local checksum_url="$2"
  local major_version="$(echo "$CODACY_REPORTER_VERSION" | cut -d '.' -f 1)"

  if [ "$CODACY_REPORTER_SKIP_CHECKSUM" = true ]; then
    log "$i" "Force skipping checksum on the binary."
  elif [ "$major_version" -ge 13 ]; then
    log "$i" "Checking checksum..."
    download_file "$checksum_url"
    
    if command -v sha512sum > /dev/null 2>&1; then
        sha_check_command="sha512sum"
    elif command -v shasum > /dev/null 2>&1; then
        sha_check_command="shasum -a 512"
    else
        fatal "Error: no method of validating checksum, please install 'sha512sum' or 'shasum'. You can skip this check by setting CODACY_REPORTER_SKIP_CHECKSUM=true"
    fi

    log "$i" "Expected checksum"
    cat "$file_name.SHA512SUM"
    log "$i" "Actual checksum"
    $sha_check_command "$file_name"
    $sha_check_command -c "$file_name.SHA512SUM"
  else
    log "$i" "Checksum not available for versions prior to 13.0.0, consider updating your CODACY_REPORTER_VERSION"
  fi
}

download() {
    local url="$1"
    local file_name="$2"
    local output_folder="$3"
    local output_filename="$4"
    local checksum_url="$5"

    pushd "$output_folder"

    download_file "$url"
    checksum "$file_name" "$checksum_url"
    mv "$file_name" "$output_filename"

    popd
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
    local reporter_folder=$2
    local reporter_filename=$3

    if [ ! -f "$reporter_path" ]
    then
        log "$i" "Downloading the codacy reporter $binary_name... ($CODACY_REPORTER_VERSION)"

        binary_url="https://artifacts.codacy.com/bin/codacy-coverage-reporter/$CODACY_REPORTER_VERSION/$binary_name"
        checksum_url="https://github.com/codacy/codacy-coverage-reporter/releases/download/$CODACY_REPORTER_VERSION/$binary_name.SHA512SUM"

        download "$binary_url" "$binary_name" "$reporter_folder" "$reporter_filename" "$checksum_url"
    else
        log "$i" "Codacy reporter $binary_name already in cache"
    fi
}

os_name=$(uname)

# Find the latest version in case is not specified
if [ -z "$CODACY_REPORTER_VERSION" ] || [ "$CODACY_REPORTER_VERSION" = "latest" ]; then
    CODACY_REPORTER_VERSION=$(download_stdout "https://artifacts.codacy.com/bin/codacy-coverage-reporter/latest")
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

download_reporter "$reporter_path" "$reporter_folder" "$reporter_filename"

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
