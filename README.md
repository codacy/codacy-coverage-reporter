# Codacy Coverage Reporter

[![Codacy Badge](https://api.codacy.com/project/badge/grade/1c524e61cd8640e79b80d406eda8754b)](https://www.codacy.com/app/Codacy/codacy-coverage-reporter)
[![Build Status](https://circleci.com/gh/codacy/codacy-coverage-reporter.png?style=shield&circle-token=:circle-token)](https://circleci.com/gh/codacy/codacy-coverage-reporter)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.codacy/codacy-coverage-reporter/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.codacy/codacy-coverage-reporter)

Multi-language coverage reporter for Codacy https://www.codacy.com

## Setup

Codacy assumes that coverage is previously configured for your project.
The supported coverage formats are JaCoCo and Cobertura.

You can run the coverage reporter:

### Linux Binary

**Operating Systems**: Linux

#### Bintray

```bash
curl -L -o codacy-coverage-reporter "https://dl.bintray.com/codacy/Binaries/$(curl https://api.bintray.com/packages/codacy/Binaries/codacy-coverage-reporter/versions/_latest | jq -r .name)/codacy-coverage-reporter"
chmod u+x codacy-coverage-reporter
./codacy-coverage-reporter report -l Java -r jacoco.xml
```

### Java Jar

**Requirements:** Java JRE 8+

**Operating Systems:** Windows, Unix (Linux, Mac OS, ...)

1. Download the latest jar from https://github.com/codacy/codacy-coverage-reporter/releases/latest
2. Run the command bellow

```bash
java -jar codacy-coverage-reporter-assembly-<version>.jar report -l Java -r jacoco.xml
```

## Updating Codacy

To update Codacy, you will need your project API token. You can find the token in Project -> Settings -> Integrations -> Project API.

Then set it in your terminal, replacing %Project_Token% with your own token:

```bash
export CODACY_PROJECT_TOKEN=%Project_Token%
```

You can also use the option `--project-token` or `-t` to set it.

### CommitUUID Detection

Codacy automatically detects the CommitUUID from several sources:

#### Environment Variables

* CI_COMMIT
* TRAVIS_PULL_REQUEST_SHA
* TRAVIS_COMMIT
* DRONE_COMMIT
* CIRCLE_SHA1
* CI_COMMIT_ID
* WERCKER_GIT_COMMIT
* CODEBUILD_RESOLVED_SOURCE_VERSION

#### Git directory

* If it finds a git directory it will get current commit.

#### Force CommitUUID

* You may want to enforce a specific commitUUID with:

```
codacy-coverage-reporter report -l Java --commit-uuid "mycommituuid" -r coverage.xml
```

### Upload coverage

Next, simply run the Codacy reporter. It will find the current commit and send all details to your project dashboard:

```bash
codacy-coverage-reporter report -l Java -r coverage.xml
```

> Note: You should keep your API token well **protected**, as it grants owner permissions to your projects.

### Upload multiple coverage reports for the same language

In order to send multiple reports for the same language, you need to upload each report separately with the flag `--partial` and then notify Codacy, after all reports were sent, with the `final` command.

***Example***

1. `codacy-coverage-reporter report -l Java -r report1.xml --partial`
2. `codacy-coverage-reporter report -l Java -r report2.xml --partial`
3. `codacy-coverage-reporter final`

If you are sending reports with the partial flag for a certain language you should use it in all reports for that language to ensure the correct calculation of the coverage.

It might also be possible to merge the reports before uploading them to Codacy, since most coverage tools support merge/aggregation, example: http://www.eclemma.org/jacoco/trunk/doc/merge-mojo.html .

### Other Languages

If your language is not in the list of supported languages, you can still send coverage to Codacy. Just provide the correct `--language` name and then add `--forceLanguage` to make sure it is sent.

### Enterprise

To send coverage in the enterprise version you should:

```bash
export CODACY_API_BASE_URL=<Codacy_instance_URL>:16006
```

Or use the option `--codacy-api-base-url <Codacy_instance_URL>:16006`.

## Other commands

For a complete list of commands and options run:

```bash
java -jar codacy-coverage-reporter-assembly-<version>.jar --help
```

## Java 6

**Alternative:** Use the `Linux Binary` described above

Due to a limitation in Java 6, the plugin is unable to establish a connection to codacy.com.
You can run [this script](https://gist.github.com/mrfyda/51cdf48fa0722593db6a) after the execution to upload the generated report to Codacy.

## Alternative usages

### Build from source

If you are having any issues with your installation, you can also build the coverage reporter from source.

1. Clone our repository https://github.com/codacy/codacy-coverage-reporter

2. Run the command `sbt assembly`. This will produce a .jar that you can run in the `codacy-coverage-reporter/target/codacy-coverage-reporter-assembly-<version>.jar`

3. In the project you want to send the coverage, use the jar. Example:

```bash
<path>/java-project$ java -jar ../codacy-coverage-reporter/target/codacy-coverage-reporter-assembly-<version>.jar report -l Java -r jacoco.xml
```

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

___
Gradle task by [Mr_ramych](https://github.com/MrRamych). Made up from solution above.

```gradle
configurations { codacy }
repositories {
    maven { url "https://jitpack.io" }
    maven { url "http://dl.bintray.com/typesafe/maven-releases" }
}
dependencies {
    codacy 'com.github.codacy:codacy-coverage-reporter:-SNAPSHOT'
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

### Community supported alternatives

#### Maven plugin

Thanks to the amazing job of [halkeye](https://github.com/halkeye) you can now send your coverage to Codacy using a [maven plugin](https://github.com/halkeye/codacy-maven-plugin)!

Just follow the [instructions on his repository](https://github.com/halkeye/codacy-maven-plugin/blob/master/README.md#usage).

#### Travis CI

If you want to use codacy with Travis CI and report coverage generated from your tests run in Travis, update your .travis.yml to include the following blocks:

```yaml
before_install:
  - sudo apt-get install jq
  - curl -o codacy-coverage-reporter "https://dl.bintray.com/codacy/Binaries/$(curl https://api.bintray.com/packages/codacy/Binaries/codacy-coverage-reporter/versions/_latest | jq -r .name)/codacy-coverage-reporter"
  - chmod u+x codacy-coverage-reporter

after_success:
  - codacy-coverage-reporter report -l Java -r build/reports/jacoco/test/jacocoTestReport.xml
```

Make sure you have set `CODACY_PROJECT_TOKEN` as an environment variable in your travis job!

## Troubleshooting

### `Failed to upload report: Not Found`

Error when running the command, then you'll probably have codacy-coverage-reporter 1.0.3 installed.
Make sure you install version 1.0.4, that fixes that error.

Example (issue: [#11](https://github.com/codacy/codacy-coverage-reporter/issues/11)) :

```log
codacy-coverage-reporter report -l Java -r PATH_TO_COVERAGE/coverage.xml
2015-11-20 04:06:58,887 [info]  com.codacy Parsing coverage data...
2015-11-20 04:06:59,506 [info]  com.codacy Uploading coverage data...

2015-11-20 04:07:00,639 [error] com.codacy Failed to upload report: Not Found
```

Even after doing all of the above troubleshooting steps in case you still encounter the same error

```log
2015-11-20 04:07:00,639 [error] com.codacy Failed to upload report: Not Found
```

Please try running the command with a --prefix option with path to your code  as shown below , it helps to locate the files for which code coverage is desired

```bash
codacy-coverage-reporter report -l Java -r PATH_TO_COVERAGE/coverage.xml --prefix PATH_TO_THE_DIRECTORY
```

Example:

```bash
codacy-coverage-reporter report -l Java -r api/target/site/jacoco/jacoco.xml --prefix api/src/main/java/
```

## What is Codacy

[Codacy](https://www.codacy.com/) is an Automated Code Review Tool that monitors your technical debt, helps you improve your code quality, teaches best practices to your developers, and helps you save time in Code Reviews.

### Among Codacy’s features

* Identify new Static Analysis issues
* Commit and Pull Request Analysis with GitHub, BitBucket/Stash, GitLab (and also direct git repositories)
* Auto-comments on Commits and Pull Requests
* Integrations with Slack, HipChat, Jira, YouTrack
* Track issues Code Style, Security, Error Proneness, Performance, Unused Code and other categories

Codacy also helps keep track of Code Coverage, Code Duplication, and Code Complexity.

Codacy supports PHP, Python, Ruby, Java, JavaScript, and Scala, among others.

### Free for Open Source

Codacy is free for Open Source projects.
