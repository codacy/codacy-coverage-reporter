# Codacy Coverage Reporter

[![Codacy Badge](https://api.codacy.com/project/badge/grade/1c524e61cd8640e79b80d406eda8754b)](https://www.codacy.com/app/Codacy/codacy-coverage-reporter)
[![Build Status](https://circleci.com/gh/codacy/codacy-coverage-reporter.png?style=shield&circle-token=:circle-token)](https://circleci.com/gh/codacy/codacy-coverage-reporter)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.codacy/codacy-coverage-reporter/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.codacy/codacy-coverage-reporter)

Multi-language coverage reporter for Codacy https://www.codacy.com

## Requirements

* Java JRE 8 and higher

## Setup

Codacy assumes that coverage is previously configured for your project.
The supported coverage formats are JaCoCo, Cobertura and LCOV.

1. Setup the project API token. You can find the token in Project -> Settings -> Integrations -> Project API.

Then set it in your terminal, replacing %Project_Token% with your own token:

```
export CODACY_PROJECT_TOKEN=%Project_Token%
```

### Using the script

Additional requirements:

* jq
* curl

```bash
bash <(curl -Ls https://coverage.codacy.com/get.sh)
```

If you want to specify a coverage reporter version, use `CODACY_REPORTER_VERSION` environment variable.

### Using binary manually

#### Linux amd64

Download the latest binary and use it to post the coverage to Codacy

##### Bintray

```bash
LATEST_VERSION="$(curl -Ls https://api.bintray.com/packages/codacy/Binaries/codacy-coverage-reporter/versions/_latest | jq -r .name)"
curl -Ls -o codacy-coverage-reporter "https://dl.bintray.com/codacy/Binaries/${LATEST_VERSION}/codacy-coverage-reporter-linux"
chmod +x codacy-coverage-reporter
echo "$(dig +short api.codacy.com | tail -n1) api.codacy.com" >> /etc/hosts
./codacy-coverage-reporter report -l Java -r build/reports/jacoco/test/jacocoTestReport.xml
```

##### GitHub

```sh
curl -Ls -o codacy-coverage-reporter "$(curl -Ls https://api.github.com/repos/codacy/codacy-coverage-reporter/releases/latest | jq -r '.assets | map({name, browser_download_url} | select(.name | contains("codacy-coverage-reporter-linux"))) | .[0].browser_download_url')"
chmod +x codacy-coverage-reporter
echo "$(dig +short api.codacy.com | tail -n1) api.codacy.com" >> /etc/hosts
./codacy-coverage-reporter report -l Java -r build/reports/jacoco/test/jacocoTestReport.xml
```

#### Others

* Linux x86, MacOS, Windows, ...

Download the latest jar and use it to post the coverage to Codacy

##### Bintray

```sh
LATEST_VERSION="$(curl -Ls https://api.bintray.com/packages/codacy/Binaries/codacy-coverage-reporter/versions/_latest | jq -r .name)"
curl -Ls -o codacy-coverage-reporter-assembly.jar "https://dl.bintray.com/codacy/Binaries/${LATEST_VERSION}/codacy-coverage-reporter-assembly.jar"
java -jar codacy-coverage-reporter-assembly.jar report -l Java -r build/reports/jacoco/test/jacocoTestReport.xml
```

##### GitHub

```sh
curl -Ls -o codacy-coverage-reporter-assembly.jar $(curl -Ls https://api.github.com/repos/codacy/codacy-coverage-reporter/releases/latest | jq -r '.assets | map({content_type, browser_download_url} | select(.content_type | contains("java-archive"))) | .[0].browser_download_url')
java -jar codacy-coverage-reporter-assembly.jar report -l Java -r jacoco.xml
```

### CommitUUID Detection

Codacy automatically detects the CommitUUID from several sources:

**Providers**

* Appveyor
* Bitrise
* Buildkite
* Circle CI
* Codefresh
* Codeship
* Docker
* Gitlab
* Greenhouse CI
* Jenkins
* Magnum CI
* Semaphore CI
* Shippable CI
* Solano CI
* TeamCity CI
* Travis CI
* Wercker CI

**Git directory**

* If it finds a git directory it will get current commit.

**Force CommitUUID**

* You may want to enforce a specific commitUUID with:
```
codacy-coverage-reporter report -l Java --commit-uuid "mycommituuid" -r coverage.xml
```

**Upload coverage**

Next, simply run the Codacy reporter. It will find the current commit and send all details to your project dashboard:

```
codacy-coverage-reporter report -l Java -r coverage.xml
```

> Note: You should keep your API token well **protected**, as it grants owner permissions to your projects.

#### Multiple coverage reports for the same language

In order to send multiple reports for the same language, you need to upload each report separately with the flag `--partial` and then notify Codacy, after all reports were sent, with the `final` command.

***Example***
1. `codacy-coverage-reporter report -l Java -r report1.xml --partial`
1. `codacy-coverage-reporter report -l Java -r report2.xml --partial`
1. `codacy-coverage-reporter final`

If you are sending reports with the partial flag for a certain language you should use it in all reports for that language to ensure the correct calculation of the coverage.

It might also be possible to merge the reports before uploading them to Codacy, since most coverage tools support merge/aggregation, example: http://www.eclemma.org/jacoco/trunk/doc/merge-mojo.html .

#### Other Languages

If your language is not in the list of supported languages, you can still send coverage to Codacy. Just provide the correct `--language` name and then add `--force-language` to make sure it is sent.

### Enterprise

To send coverage in the enterprise version you should:
```
export CODACY_API_BASE_URL=<Codacy_instance_URL>:16006
```

Or use the option `--codacy-api-base-url <Codacy_instance_URL>:16006`.

## Other commands

For a complete list of commands and options run:
```
java -jar codacy-coverage-reporter-<version>-assembly.jar --help
```

## Java 6

Due to a limitation in Java 6, the plugin is unable to establish a connection to codacy.com.
You can run [this script](https://gist.github.com/mrfyda/51cdf48fa0722593db6a) after the execution to upload the generated report to Codacy.

## Build from source

If you are having any issues with your installation, you can also build the coverage reporter from source.

1. Clone our repository https://github.com/codacy/codacy-coverage-reporter

2. Run the command `sbt assembly`. This will produce a .jar that you can run in the `codacy-coverage-reporter/target/codacy-coverage-reporter-<version>-assembly.jar`

3. In the project you want to send the coverage, use the jar. Example:

```
<path>/java-project$ java -jar ../codacy-coverage-reporter/target/codacy-coverage-reporter-<version>-assembly.jar report -l Java -r jacoco.xml
```

## Gradle task

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

___
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

## Community supported alternatives

### Maven plugin

Thanks to the amazing job of [halkeye](https://github.com/halkeye) you can now send your coverage to Codacy using a [maven plugin](https://github.com/halkeye/codacy-maven-plugin)!

Just follow the [instructions on his repository](https://github.com/halkeye/codacy-maven-plugin/blob/master/README.md#usage).

### Travis CI

If you want to use codacy with Travis CI and report coverage generated from your tests run in Travis, update your .travis.yml to include the following blocks:

```yaml
before_install:
  - sudo apt-get install jq
  - curl -LSs $(curl -LSs https://api.github.com/repos/codacy/codacy-coverage-reporter/releases/latest | jq -r '.assets | map({content_type, browser_download_url} | select(.content_type | contains("application/java-archive"))) | .[0].browser_download_url') -o codacy-coverage-reporter-assembly.jar

after_success:
  - java -jar codacy-coverage-reporter-assembly.jar report -l Java -r build/reports/jacoco/test/jacocoTestReport.xml
```

Make sure you have set `CODACY_PROJECT_TOKEN` as an environment variable in your travis job!

## Troubleshooting

### `Failed to upload report: Not Found`

Error when running the command, then you'll probably have codacy-coverage-reporter 1.0.3 installed.
Make sure you install version 1.0.4, that fixes that error.

Example (issue: [#11](https://github.com/codacy/codacy-coverage-reporter/issues/11)) :
```
codacy-coverage-reporter report -l Java -r PATH_TO_COVERAGE/coverage.xml
2015-11-20 04:06:58,887 [info]  com.codacy Parsing coverage data...
2015-11-20 04:06:59,506 [info]  com.codacy Uploading coverage data...

2015-11-20 04:07:00,639 [error] com.codacy Failed to upload report: Not Found
```
Even after doing all of the above troubleshooting steps in case you still encounter the same error

```
2015-11-20 04:07:00,639 [error] com.codacy Failed to upload report: Not Found
```

Please try running the command with a --prefix option with path to your code  as shown below , it helps to locate the files for which code coverage is desired

```
codacy-coverage-reporter report -l Java -r PATH_TO_COVERAGE/coverage.xml --prefix PATH_TO_THE_DIRECTORY
```

Example

```
codacy-coverage-reporter report -l Java -r api/target/site/jacoco/jacoco.xml --prefix api/src/main/java/
```

## What is Codacy?

[Codacy](https://www.codacy.com/) is an Automated Code Review Tool that monitors your technical debt, helps you improve your code quality, teaches best practices to your developers, and helps you save time in Code Reviews.

### Among Codacy’s features:

 - Identify new Static Analysis issues
 - Commit and Pull Request Analysis with GitHub, BitBucket/Stash, GitLab (and also direct git repositories)
 - Auto-comments on Commits and Pull Requests
 - Integrations with Slack, HipChat, Jira, YouTrack
 - Track issues Code Style, Security, Error Proneness, Performance, Unused Code and other categories

Codacy also helps keep track of Code Coverage, Code Duplication, and Code Complexity.

Codacy supports PHP, Python, Ruby, Java, JavaScript, and Scala, among others.

### Free for Open Source

Codacy is free for Open Source projects.
