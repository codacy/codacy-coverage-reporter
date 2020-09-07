# Alternative ways of running Coverage Reporter

The recommended way to run Codacy Coverage Reporter is using a self-contained script that automatically downloads and runs the most recent version of Codacy Coverage Reporter.

On Ubuntu, run:

```bash
bash <(curl -Ls https://coverage.codacy.com/get.sh)
```

On Alpine Linux, run:

```sh
wget -qO - https://coverage.codacy.com/get.sh | sh
```

To use a specific version of the Codacy Coverage Reporter, set the following environment variable to one of the [released versions](https://github.com/codacy/codacy-coverage-reporter/releases):

```bash
export CODACY_REPORTER_VERSION=<version>
```

The sections below provide details on alternative ways to run or install Codacy Coverage Reporter.

## CircleCI orb

If you are using CircleCI to report coverage, you can use our orb [codacy/coverage-reporter](https://circleci.com/orbs/registry/orb/codacy/coverage-reporter).

## GitHub Action

If you are using GitHub Actions to report coverage, you can use our GitHub Action [codacy/codacy-coverage-reporter-action](https://github.com/codacy/codacy-coverage-reporter-action).

## Community supported alternatives

### Maven plugin

Thanks to the amazing job of [Gavin Mogan](https://github.com/halkeye) you can now send your coverage to Codacy using his Maven plugin [halkeye/codacy-maven-plugin](https://github.com/halkeye/codacy-maven-plugin)! Be sure to follow the instructions on his repository.

### Travis CI

If you are using Travis CI to report coverage, update your file `.travis.yml` to include the following blocks:

```yaml
before_install:
  - sudo apt-get install jq
  - curl -LSs "$(curl -LSs https://api.github.com/repos/codacy/codacy-coverage-reporter/releases/latest | jq -r '.assets | map({name, browser_download_url} | select(.name | endswith(".jar"))) | .[0].browser_download_url')" -o codacy-coverage-reporter-assembly.jar

after_success:
  - java -jar codacy-coverage-reporter-assembly.jar report -l Java -r build/reports/jacoco/test/jacocoTestReport.xml
```

Make sure that you also [set your project or account API Token](adding-coverage-to-your-repository.md#authenticate) as an environment variable in your Travis CI job.

### Gradle task

A big shout-out to [Tom Howard](https://github.com/tompahoward), who suggested a way to [create a Gradle task](https://github.com/mountain-pass/hyperstate/commit/857ca93e1c8484c14a5e2da9f0434d3daf3328ce).

```gradle
task uploadCoverageToCodacy(type: JavaExec, dependsOn : jacocoTestReport) {
   main = "com.codacy.CodacyCoverageReporter"
   classpath = configurations.codacy
   args = [
            "report",
            "-l",
            "Java",
            "-r",
            "${buildDir}/test-results/jacoco/${archivesBaseName}.xml"
           ]
}

task (codacyDepsize) << {
def size = 0;
configurations.codacy.collect { it.length() / (1024 * 1024) }.each { size += it }
println "Total dependencies size: ${Math.round(size * 100) / 100} Mb"

configurations
        .codacy
        .sort { -it.length() }
        .each { println "${it.name} : ${Math.round(it.length() / (1024) * 100) / 100} kb" }
}

task (codacyLocs) << {
    configurations.codacy.each {
        String jarName = it
        println jarName
    }
}
```

The following Gradle task by [MrRamych](https://github.com/MrRamych) was based on the solution above.

```gradle
configurations { codacy }
repositories {
    jcenter()
}
dependencies {
    codacy 'com.codacy:codacy-coverage-reporter:latest.release'
}
task sendCoverageToCodacy(type: JavaExec, dependsOn: jacocoTestReport) {
    main = "com.codacy.CodacyCoverageReporter"
    classpath = configurations.codacy
    args = [
            "report",
            "-l",
            "Java",
            "-r",
            "${buildDir}/reports/jacoco/test/jacocoTestReport.xml"
    ]
}
```

## Manually downloading the native Linux amd64 binary

If you prefer, you can manually download and run the native `codacy-coverage-reporter` binary, either for the latest version or a specific one.

You can use the scripts below to automatically check for the latest version of the binaries, download the binaries from either Bintray or GitHub, and run them.

Using Bintray:

```bash
LATEST_VERSION="$(curl -Ls https://api.bintray.com/packages/codacy/Binaries/codacy-coverage-reporter/versions/_latest | jq -r .name)"
curl -Ls -o codacy-coverage-reporter "https://dl.bintray.com/codacy/Binaries/${LATEST_VERSION}/codacy-coverage-reporter-linux"
chmod +x codacy-coverage-reporter
./codacy-coverage-reporter report
```

Using GitHub:

```bash
curl -Ls -o codacy-coverage-reporter "$(curl -Ls https://api.github.com/repos/codacy/codacy-coverage-reporter/releases/latest | jq -r '.assets | map({name, browser_download_url} | select(.name | contains("codacy-coverage-reporter-linux"))) | .[0].browser_download_url')"
chmod +x codacy-coverage-reporter
./codacy-coverage-reporter report
```

## Manually downloading the Java binary

Use the Java binary to run Codacy Coverage reporter on other platforms, such as Linux x86, MacOS, Windows, etc.

You can use the scripts below to automatically check for the latest version of the Java binaries, download the binaries from either Bintray or GitHub, and run them.

Using Bintray:

```bash
LATEST_VERSION="$(curl -Ls https://api.bintray.com/packages/codacy/Binaries/codacy-coverage-reporter/versions/_latest | jq -r .name)"
curl -Ls -o codacy-coverage-reporter-assembly.jar "https://dl.bintray.com/codacy/Binaries/${LATEST_VERSION}/codacy-coverage-reporter-assembly.jar"
java -jar codacy-coverage-reporter-assembly.jar report
```

Using GitHub:

```bash
curl -LS -o codacy-coverage-reporter-assembly.jar "$(curl -LSs https://api.github.com/repos/codacy/codacy-coverage-reporter/releases/latest | jq -r '.assets | map({name, browser_download_url} | select(.name | endswith(".jar"))) | .[0].browser_download_url')"
java -jar codacy-coverage-reporter-assembly.jar report
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
