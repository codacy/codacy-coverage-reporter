---
description: There are alternative ways of running or installing Codacy Coverage Reporter, such as running a Docker image, using a GitHub Action or CircleCI orb, downloading a binary for your operating system, or building the binary from source.
---

# Alternative ways of running Coverage Reporter

The following sections list the alternative ways of running or installing Codacy Coverage Reporter.

## Bash script (recommended) {: id="bash-script"}

The recommended way to run the Codacy Coverage Reporter is by using the [self-contained bash script `get.sh`](https://github.com/codacy/codacy-coverage-reporter/blob/master/get.sh) that automatically downloads and runs the most recent version of the Codacy Coverage Reporter:

-   On Ubuntu, run:

    ```bash
    bash <(curl -Ls https://coverage.codacy.com/get.sh) report -r <coverage report file name>
    ```

-   On Alpine Linux, run:

    ```sh
    wget -q https://coverage.codacy.com/get.sh
    sh get.sh report -r <coverage report file name>
    rm get.sh
    ```

!!! note
    Starting on version `13.0.0` the script automatically validates the checksum of the downloaded binary. To skip the checksum validation, define the following environment variable:

    ```bash
    export CODACY_REPORTER_SKIP_CHECKSUM=true
    ```

The self-contained script can cache the binary. To avoid downloading the binary every time that the script runs, add one of the following directories to your CI cached folders:

-   `$HOME/.cache/codacy` on Linux
-   `$HOME/Library/Caches/Codacy` on Mac OS X

To use a specific version of the Codacy Coverage Reporter, set the following environment variable to one of the [released versions](https://github.com/codacy/codacy-coverage-reporter/releases):

```bash
export CODACY_REPORTER_VERSION=<version>
```

## Docker

You can use Docker to run Codacy Coverage Reporter.

Use the following command where `<version>` is either one of the [released versions](https://github.com/codacy/codacy-coverage-reporter/releases), or `latest` to use the most recent version:

```bash
docker run -v $PWD:/code codacy/codacy-coverage-reporter:<version> report
```

## GitHub Action

If you're using GitHub Actions to report coverage, you can use our GitHub Action [codacy/codacy-coverage-reporter-action](https://github.com/codacy/codacy-coverage-reporter-action).

## CircleCI orb

If you're using CircleCI to report coverage, you can use our orb [codacy/coverage-reporter](https://circleci.com/orbs/registry/orb/codacy/coverage-reporter).

## Manually downloading the binary

### Linux amd64

If you prefer, you can manually download and run the native `codacy-coverage-reporter` binary, either for the latest version or a specific one.

You can use the scripts below to automatically check for the latest version of the binaries, download the binaries from either Codacy's public store or GitHub, and run them.

-   Using Codacy's public S3:

    ```bash
    LATEST_VERSION="$(curl -Ls https://artifacts.codacy.com/bin/codacy-coverage-reporter/latest)"
    curl -Ls -o codacy-coverage-reporter "https://artifacts.codacy.com/bin/codacy-coverage-reporter/${LATEST_VERSION}/codacy-coverage-reporter-linux"
    chmod +x codacy-coverage-reporter
    ./codacy-coverage-reporter report
    ```

-   Using GitHub:

    ```bash
    curl -Ls -o codacy-coverage-reporter "$(curl -Ls https://api.github.com/repos/codacy/codacy-coverage-reporter/releases/latest | jq -r '.assets | map({name, browser_download_url} | select(.name | contains("codacy-coverage-reporter-linux"))) | .[0].browser_download_url')"
    chmod +x codacy-coverage-reporter
    ./codacy-coverage-reporter report
    ```

### Java 8

Use the Java 8 binary to run Codacy Coverage reporter on other platforms, such as Linux x86, macOS, Windows, etc.

You can use the scripts below to automatically check for the latest version of the Java binaries, download the binaries from either Codacy's public store or GitHub, and run them.

-   Using Codacy's public store:

    ```bash
    LATEST_VERSION="$(curl -Ls https://artifacts.codacy.com/bin/codacy-coverage-reporter/latest)"
    curl -Ls -o codacy-coverage-reporter-assembly.jar "https://artifacts.codacy.com/bin/codacy-coverage-reporter/${LATEST_VERSION}/codacy-coverage-reporter-assembly.jar"
    java -jar codacy-coverage-reporter-assembly.jar report
    ```

-   Using GitHub:

    ```bash
    curl -LS -o codacy-coverage-reporter-assembly.jar "$(curl -LSs https://api.github.com/repos/codacy/codacy-coverage-reporter/releases/latest | jq -r '.assets | map({name, browser_download_url} | select(.name | endswith(".jar"))) | .[0].browser_download_url')"
    java -jar codacy-coverage-reporter-assembly.jar report
    ```

### Validating the checksum of the binaries

You can use the checksums [available for each release](https://github.com/codacy/codacy-coverage-reporter/releases) to validate the binaries that you download manually. You can use any tool of your choice to validate the checksum, as long as it uses the `SHA512` algorithm.

For example, run the commands below to download and validate the checksum for the 13.0.0 Linux binary. Note that the command `sha512sum` expects to find the binary on the same directory and with the original name `codacy-coverage-reporter-linux`.

```bash
curl -Ls -O https://github.com/codacy/codacy-coverage-reporter/releases/download/13.0.0/codacy-coverage-reporter-linux.SHA512SUM
sha512sum -c codacy-coverage-reporter-linux.SHA512SUM
```

## Building from source

If you are having any issues with your installation, you can also build the coverage reporter from source.

1.  Clone the Codacy Coverage Reporter repository:

    ```bash
    git clone https://github.com/codacy/codacy-coverage-reporter.git
    ```

1.  Run the command `sbt assembly` inside the local repository folder:

    ```bash
    cd codacy-coverage-reporter
    sbt assembly
    ```
   
    This will produce a file `target/codacy-coverage-reporter-assembly-<version>.jar` that you can run.

1.  Execute this `.jar` in the repository where you want to upload the coverage. For example:

    ```bash
    <path>/java-project$ java -jar ../codacy-coverage-reporter/target/codacy-coverage-reporter-assembly-<version>.jar report
    ```

## Community supported alternatives

### Maven plugin

Thanks to the amazing job of [Gavin Mogan](https://github.com/halkeye) you can now send your coverage to Codacy using his Maven plugin [halkeye/codacy-maven-plugin](https://github.com/halkeye/codacy-maven-plugin)! Be sure to follow the instructions on his repository.

### Travis CI

If you are using Travis CI to report coverage, update your file `.travis.yml` to include the following blocks:

```yaml
before_script:
  - bash <(curl -Ls https://coverage.codacy.com/get.sh) download

after_success:
  - bash <(curl -Ls https://coverage.codacy.com/get.sh)
```

Make sure that you also [set your project or account API Token](index.md#authenticate) as an environment variable in your Travis CI job.

### Gradle task

If you're using Gradle to automate your CI/CD you can add use the following example task, where `<COVERAGE_REPORT_TASK>` is the name of the task that generates your coverage report:

```groovy
task uploadCoverage(type:Exec, dependsOn: <COVERAGE_REPORT_TASK>) {
    description 'Uploads coverage data to Codacy.'
    commandLine 'bash', '-c', 'bash <(curl -Ls https://coverage.codacy.com/get.sh) report'
}
```
