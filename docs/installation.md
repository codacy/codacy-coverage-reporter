# Installation

## Automatic script

Requirements: bash, curl, glibc.

```bash
bash <(curl -Ls https://coverage.codacy.com/get.sh)
```

To specify a coverage reporter version, use `CODACY_REPORTER_VERSION` environment variable.

## CircleCI orb

If you want to use Codacy with CircleCI and report coverage generated from your tests ran in CircleCI, you can use our [coverage reporter orb](https://circleci.com/orbs/registry/orb/codacy/coverage-reporter):

Example:

    jobs:
      send-coverage-report:
        steps:
          - checkout
          - "run commands to generate the coverage result"
          - coverage-reporter/send_report
    workflows:
      version: 2
      coverage-example:
        jobs:
          - send-coverage-report

## Community supported alternatives

### Maven plugin

Thanks to the amazing job of [halkeye](https://github.com/halkeye) you can now send your coverage to Codacy using a [maven plugin](https://github.com/halkeye/codacy-maven-plugin)!

Just follow the [instructions on his repository](https://github.com/halkeye/codacy-maven-plugin/blob/master/README.md#usage).

### Travis CI

If you want to use codacy with Travis CI and report coverage generated from your tests run in Travis, update your .travis.yml to include the following blocks:

```yaml
before_install:
  - sudo apt-get install jq
  - curl -LSs "$(curl -LSs https://api.github.com/repos/codacy/codacy-coverage-reporter/releases/latest | jq -r '.assets | map({name, browser_download_url} | select(.name | endswith(".jar"))) | .[0].browser_download_url')" -o codacy-coverage-reporter-assembly.jar

after_success:
  - java -jar codacy-coverage-reporter-assembly.jar report -l Java -r build/reports/jacoco/test/jacocoTestReport.xml
```

Make sure you have set `CODACY_PROJECT_TOKEN` as an environment variable in your travis job!

### Gradle task

A big shout-out to [tompahoward](https://github.com/tompahoward), you can create a gradle task as suggested in https://github.com/mountain-pass/hyperstate/commit/857ca93e1c8484c14a5e2da9f0434d3daf3328ce

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

---

Gradle task by [Mr_ramych](https://github.com/MrRamych). Made up from solution above.

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

## Manually downloading the native binary

### Linux amd64

Download the latest binary and use it to post the coverage to Codacy

#### Bintray

```bash
LATEST_VERSION="$(curl -Ls https://api.bintray.com/packages/codacy/Binaries/codacy-coverage-reporter/versions/_latest | jq -r .name)"
curl -Ls -o codacy-coverage-reporter "https://dl.bintray.com/codacy/Binaries/${LATEST_VERSION}/codacy-coverage-reporter-linux"
chmod +x codacy-coverage-reporter
./codacy-coverage-reporter report -l Java -r build/reports/jacoco/test/jacocoTestReport.xml
```

#### GitHub

```sh
curl -Ls -o codacy-coverage-reporter "$(curl -Ls https://api.github.com/repos/codacy/codacy-coverage-reporter/releases/latest | jq -r '.assets | map({name, browser_download_url} | select(.name | contains("codacy-coverage-reporter-linux"))) | .[0].browser_download_url')"
chmod +x codacy-coverage-reporter
./codacy-coverage-reporter report -l Java -r build/reports/jacoco/test/jacocoTestReport.xml
```

If you are experiencing segmentation faults uploading the coverage (due to [oracle/graal#624](https://github.com/oracle/graal/issues/624)), do this before running the reporter, as a workaround:

```sh
echo "$(dig +short api.codacy.com | tail -n1) api.codacy.com" >> /etc/hosts
```

## Manually downloading the Java binary

- Linux x86, MacOS, Windows, ...

Download the latest jar and use it to post the coverage to Codacy

#### Bintray

```sh
LATEST_VERSION="$(curl -Ls https://api.bintray.com/packages/codacy/Binaries/codacy-coverage-reporter/versions/_latest | jq -r .name)"
curl -Ls -o codacy-coverage-reporter-assembly.jar "https://dl.bintray.com/codacy/Binaries/${LATEST_VERSION}/codacy-coverage-reporter-assembly.jar"
java -jar codacy-coverage-reporter-assembly.jar report -l Java -r build/reports/jacoco/test/jacocoTestReport.xml
```

#### GitHub

```sh
curl -LS -o codacy-coverage-reporter-assembly.jar "$(curl -LSs https://api.github.com/repos/codacy/codacy-coverage-reporter/releases/latest | jq -r '.assets | map({name, browser_download_url} | select(.name | endswith(".jar"))) | .[0].browser_download_url')"
java -jar codacy-coverage-reporter-assembly.jar report -l Java -r jacoco.xml
```

## Build from source

If you are having any issues with your installation, you can also build the coverage reporter from source.

1. Clone our repository https://github.com/codacy/codacy-coverage-reporter

2. Run the command `sbt assembly`. This will produce a .jar that you can run in the `codacy-coverage-reporter/target/codacy-coverage-reporter-<version>-assembly.jar`

3. In the project you want to send the coverage, use the jar. Example:

```
<path>/java-project$ java -jar ../codacy-coverage-reporter/target/codacy-coverage-reporter-<version>-assembly.jar report -l Java -r jacoco.xml
```
