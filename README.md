# Codacy Coverage Reporter

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/1c524e61cd8640e79b80d406eda8754b)](https://www.codacy.com/gh/codacy/codacy-coverage-reporter?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=codacy/codacy-coverage-reporter&amp;utm_campaign=Badge_Grade)
[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/1c524e61cd8640e79b80d406eda8754b)](https://www.codacy.com/gh/codacy/codacy-coverage-reporter?utm_source=github.com&utm_medium=referral&utm_content=codacy/codacy-coverage-reporter&utm_campaign=Badge_Coverage)
[![Build Status](https://circleci.com/gh/codacy/codacy-coverage-reporter.png?style=shield&circle-token=:circle-token)](https://circleci.com/gh/codacy/codacy-coverage-reporter)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.codacy/codacy-coverage-reporter/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.codacy/codacy-coverage-reporter)

Multi-language coverage reporter for Codacy https://www.codacy.com

## Setup

1. Copy a *Project API Token*. You can find the token within a repository *Settings* → *Integrations* → *Project API*.
1. Set the *Project API Token* in your terminal, replacing %Project_Token% with your own:

```
export CODACY_PROJECT_TOKEN=%Project_Token%
```

⚠️ **You should keep your API token well protected, as it grants owner permissions to your projects.**

> If you'd like to automate this process for multiple repositories you can [authenticate using an Account API Token](docs/authentication.md).

### Self-hosted

To upload coverage to a self-hosted installation of Codacy you need to set your installation URL:

```
export CODACY_API_BASE_URL=<Codacy_instance_URL>:16006
```

Or use the flag: `--codacy-api-base-url <Codacy_instance_URL>:16006`.

## Running Codacy Coverage Reporter

### Requirements 

- `bash` or `sh` (Use `bash` on Ubuntu)
- `curl` or `wget`
- `glibc`

### _Using curl_
```bash
bash <(curl -Ls https://coverage.codacy.com/get.sh)
```

### _Using wget_
```bash
bash <(wget -q -O - https://coverage.codacy.com/get.sh)
```

### _On Alpine Linux_
```sh
wget -qO - https://coverage.codacy.com/get.sh | sh
```

#### _On Alpine Linux passsing command line options_
```sh
wget -qO - https://coverage.codacy.com/get.sh | sh -s report -l Java -r report1.xml --partial
```

### Choose a specific version
The script uses by default the latest version.

If you want to specify a coverage reporter version, use `CODACY_REPORTER_VERSION` environment variable.

You can find all versions in the [Releases tab](https://github.com/codacy/codacy-coverage-reporter/releases).

### Manual Installation

Using GitHub Actions? Check out the [codacy/coverage-coverage-reporter action](https://github.com/codacy/codacy-coverage-reporter-action#codacy-coverage-reporter-action).

If the automated script does not cover your use case, [check the manual installation methods](docs/installation.md#manually-downloading-the-native-binary).

## CircleCI Orb

Using CircleCI? Check out the [codacy/coverage-reporter orb](docs/installation.md#circleci-orb).

## Supported formats

The following table contains the formats supported and which coverage tools generate them:

| Language   | Coverage tools (examples) | Formats   | Filename |
| ---        | ---                       | ---       | ---      |
| Java       | [JaCoCo](http://eclemma.org/jacoco/) <br> [Cobertura](http://cobertura.github.io/cobertura/) | JaCoCo <br> Cobertura | jacoco*.xml <br> cobertura.xml |
| Scala      | [sbt-jacoco](https://www.scala-sbt.org/sbt-jacoco/) <br> [scoverage](http://scoverage.org/) | JaCoCo <br> Cobertura | jacoco*.xml <br> cobertura.xml |
| Javascript | [Istanbul](https://github.com/gotwarlost/istanbul) <br> [Poncho](https://github.com/deepsweet/poncho) <br> [Mocha](http://mochajs.org/) + [Blanket.js](https://github.com/alex-seville/blanket) | LCOV | lcov.info, lcov.dat, *.lcov |
| Python     | [Coverage.py](https://coverage.readthedocs.io/en/coverage-5.0.3/) | Cobertura                 | cobertura.xml |
| PHP        | [PHPUnit](https://phpunit.readthedocs.io/en/8.5/code-coverage-analysis.html) | PHPUnit XML <br> [Clover](https://confluence.atlassian.com/clover/using-clover-for-php-420973033.html) | coverage-xml/index.xml <br> clover.xml |
| Ruby       | [SimpleCov](https://github.com/colszowka/simplecov) | [Cobertura](https://github.com/dashingrocket/simplecov-cobertura) <br> [LCOV](https://github.com/fortissimo1997/simplecov-lcov) | cobertura.xml <br> lcov.info, lcov.dat, *.lcov |
| C#         | [OpenCover](https://github.com/OpenCover/opencover) <br> [DotCover CLI](https://www.jetbrains.com/dotcover/) | OpenCover <br> DotCover-DetailedXML | opencover.xml <br> dotcover.xml |
| Swift/Objective-C     | XCode Coverage | [Check here information about reports for this language](docs/swift_objectivec.md) |  |

The reporter assumes the coverage reports filename follow the name convention. Otherwise, you must define the report's location with the flag `-r`.

> If your coverage reports are in a different format you can use a format converter, such as [ReportGenerator](https://danielpalme.github.io/ReportGenerator/), to generate a supported format.

### Unsuported Languages

If your language is not in the list of supported languages, you can still send coverage to Codacy. You can do it by providing the correct `--language` name and then add the `--force-language` flag.

## Commit SHA hash detection

Codacy automatically detects a commit SHA hash from CI workflows, the git repository or command line arguments. See [all supported environments](docs/commit_detection.md).

## Multiple coverage reports for the same language

In order to send multiple reports for the same language, you need to upload each report separately with the flag `--partial` and then notify Codacy, after all reports were sent, with the `final` command.

**_Example_**

1. `codacy-coverage-reporter report -l Java -r report1.xml --partial`
1. `codacy-coverage-reporter report -l Java -r report2.xml --partial`
1. `codacy-coverage-reporter final`

**_Using the script_**

1. `bash <(curl -Ls https://coverage.codacy.com/get.sh) report -l Java -r report1.xml --partial`
1. `bash <(curl -Ls https://coverage.codacy.com/get.sh) report -l Java -r report2.xml --partial`
1. `bash <(curl -Ls https://coverage.codacy.com/get.sh) final`

If you are sending reports with the partial flag for a certain language you should use it in all reports for that language to ensure the correct calculation of the coverage.

It might also be possible to merge the reports before uploading them to Codacy, since most coverage tools support merge/aggregation, example: http://www.eclemma.org/jacoco/trunk/doc/merge-mojo.html.

## Other commands

For a complete list of commands and options: `--help`

## Troubleshooting

### JsonParseException while uploading C# coverage data

If you're using dotCover to generate coverage reports for your C# projects, you should [filter out xUnit files](https://www.jetbrains.com/help/dotcover/Running_Coverage_Analysis_from_the_Command_LIne.html#filters_cmd) from the coverage analysis as follows:

```bash
dotCover.exe cover ... /Filters=-:xunit*
```

By default, dotCover includes xUnit files in the coverage analysis and this results in larger coverage reports. This filter helps ensure that the resulting coverage data does not exceed the size limit accepted by the Codacy API when uploading the results.

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

Please try running the command with a --prefix option with path to your code as shown below , it helps to locate the files for which code coverage is desired

```
codacy-coverage-reporter report -l Java -r PATH_TO_COVERAGE/coverage.xml --prefix PATH_TO_THE_DIRECTORY
```

Example

```
codacy-coverage-reporter report -l Java -r api/target/site/jacoco/jacoco.xml --prefix api/src/main/java/
```

### `SubstrateSegfaultHandler caught signal 11`

If you are experiencing segmentation faults uploading the coverage (due to [oracle/graal#624](https://github.com/oracle/graal/issues/624)), do this before running the reporter, as a workaround:

```sh
echo "$(dig +short api.codacy.com | tail -n1) api.codacy.com" >> /etc/hosts
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
