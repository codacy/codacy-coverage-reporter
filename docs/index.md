# Getting started

## Authentication

1. Find and copy a *Project API Token*. You can find the token within a repository *Settings* → *Integrations* → *Project API*.
1. Set the *Project API Token* in your terminal, replacing `%Project_Token%` with your own:

```bash
export CODACY_PROJECT_TOKEN=%Project_Token%
```

!!! warning
    You should keep your API token well protected, as it grants owner permissions to your projects.

!!! hint
    If you'd like to automate this process for multiple repositories you can [authenticate using an Account API Token](advanced/authenticating-using-account-token.md).

!!! note
    To upload coverage to a self-hosted installation of Codacy you need to set your installation URL:

    ```bash
    export CODACY_API_BASE_URL=<Codacy_instance_URL>:16006
    ```

    Or use the flag: `--codacy-api-base-url <Codacy_instance_URL>:16006`.

## Running Codacy Coverage Reporter

The easiest way to get starting is by using the self-contained script that downloads and runs the reporter:

```bash
bash <(curl -Ls https://coverage.codacy.com/get.sh)
```

### Choose a specific version
The script uses by default the latest version.

If you want to specify a coverage reporter version, use `CODACY_REPORTER_VERSION` environment variable.

You can find all versions in the [Releases tab](https://github.com/codacy/codacy-coverage-reporter/releases).

### Manual Installation

Using CircleCI? Check out the [codacy/coverage-reporter orb](advanced/installation-methods.md#circleci-orb).

Using GitHub Actions? Check out the [codacy/coverage-coverage-reporter action](https://github.com/codacy/codacy-coverage-reporter-action#codacy-coverage-reporter-action).

If the automated script does not cover your use case, [check the manual installation methods](advanced/installation-methods.md#manually-downloading-the-native-binary).

## Supported formats

The following table contains the formats supported and which coverage tools generate them:

| Language          | Coverage tools (examples)                                                                                                                                                                       | Formats                                                                                                                         | Filename                                       |
| ----------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------- |
| Java              | [JaCoCo](http://eclemma.org/jacoco/) <br> [Cobertura](http://cobertura.github.io/cobertura/)                                                                                                    | JaCoCo <br> Cobertura                                                                                                           | jacoco*.xml <br> cobertura.xml                 |
| Scala             | [sbt-jacoco](https://www.scala-sbt.org/sbt-jacoco/) <br> [scoverage](http://scoverage.org/)                                                                                                     | JaCoCo <br> Cobertura                                                                                                           | jacoco*.xml <br> cobertura.xml                 |
| Javascript        | [Istanbul](https://github.com/gotwarlost/istanbul) <br> [Poncho](https://github.com/deepsweet/poncho) <br> [Mocha](http://mochajs.org/) + [Blanket.js](https://github.com/alex-seville/blanket) | LCOV                                                                                                                            | lcov.info, lcov.dat, *.lcov                    |
| Python            | [Coverage.py](https://coverage.readthedocs.io/en/coverage-5.0.3/)                                                                                                                               | Cobertura                                                                                                                       | cobertura.xml                                  |
| PHP               | [PHPUnit](https://phpunit.readthedocs.io/en/8.5/code-coverage-analysis.html)                                                                                                                    | PHPUnit XML (version <= 4) <br> [Clover](https://confluence.atlassian.com/clover/using-clover-for-php-420973033.html)           | coverage-xml/index.xml <br> clover.xml         |
| Ruby              | [SimpleCov](https://github.com/colszowka/simplecov)                                                                                                                                             | [Cobertura](https://github.com/dashingrocket/simplecov-cobertura) <br> [LCOV](https://github.com/fortissimo1997/simplecov-lcov) | cobertura.xml <br> lcov.info, lcov.dat, *.lcov |
| C#                | [OpenCover](https://github.com/OpenCover/opencover) <br> [DotCover CLI](https://www.jetbrains.com/dotcover/)                                                                                    | OpenCover <br> DotCover-DetailedXML                                                                                             | opencover.xml <br> dotcover.xml                |
| Swift/Objective-C | XCode Coverage                                                                                                                                                                                  | [Check here information about reports for this language](troubleshooting/swift-objectivec.md)                                   |                                                |

The reporter assumes the coverage reports filename follow the name convention. Otherwise, you must define the report's location with the flag `-r`.

!!! note
    If your coverage reports are in a different format you can use a format converter, such as [ReportGenerator](https://danielpalme.github.io/ReportGenerator/), to generate a supported format.

### Unsuported Languages

If your language is not in the list of supported languages, you can still send coverage to Codacy. You can do it by providing the correct `--language` name and then add the `--force-language` flag.

## Commit SHA hash detection

Codacy automatically detects a commit SHA hash from CI workflows, the git repository or command line arguments. See [all supported environments](troubleshooting/commit-detection.md).

## Multiple coverage reports for the same language

You can upload multiple reports if your test suite is split in different modules or ran in parallel. See [how to uploasd multiple coverage reports](advanced/multiple-reports.md).

## Other commands

For a complete list of commands and options: `--help`
