# Adding coverage to your repository

Code coverage is a metric used to describe the degree to which the source code of a program is tested. A program with high code coverage has been more thoroughly tested and has a lower chance of containing software bugs than a program with low code coverage.

To configure your repository to show code coverage reports directly in Codacy you must complete these main steps:

1.  **Generating coverage for your repository**

    To display coverage on Codacy you must have tests and generate coverage reports for your repository.

1.  **Uploading coverage reports to Codacy**

    The tool `codacy-coverage-reporter` reads the generated coverage reports, converts them to a smaller JSON file, and uploads the file to Codacy.

The next sections include detailed instructions on how to complete each step to add coverage to your repository.

## 1. Generating coverage for your repository

There are many tools that you can use to generate coverage for your projects. The following table contains the coverage report formats that Codacy supports and which coverage tools generate the reports:

| Language          | Example coverage tools                                                                                                                                                                        | Report formats                                                                                                                 | Report filenames                               |
| ----------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------ | ---------------------------------------------- |
| Java              | [JaCoCo](http://eclemma.org/jacoco/)<br/>[Cobertura](http://cobertura.github.io/cobertura/)                                                                                                   | JaCoCo<br/>Cobertura                                                                                                           | jacoco\*.xml<br/>cobertura.xml                 |
| Scala             | [sbt-jacoco](https://www.scala-sbt.org/sbt-jacoco/)<br/>[scoverage](http://scoverage.org/)                                                                                                    | JaCoCo<br/>Cobertura                                                                                                           | jacoco\*.xml<br/>cobertura.xml                 |
| Javascript        | [Istanbul](https://github.com/gotwarlost/istanbul)<br/>[Poncho](https://github.com/deepsweet/poncho)<br/>[Mocha](http://mochajs.org/) + [Blanket.js](https://github.com/alex-seville/blanket) | LCOV                                                                                                                           | lcov.info, lcov.dat, \*.lcov                   |
| Python            | [Coverage.py](https://coverage.readthedocs.io/en/coverage-5.0.3/)                                                                                                                             | Cobertura                                                                                                                      | cobertura.xml                                  |
| PHP               | [PHPUnit](https://phpunit.readthedocs.io/en/8.5/code-coverage-analysis.html)                                                                                                                  | PHPUnit XML (version &lt;= 4)<br/>[Clover](https://confluence.atlassian.com/clover/using-clover-for-php-420973033.html)        | coverage-xml/index.xml<br/>clover.xml          |
| Ruby              | [SimpleCov](https://github.com/colszowka/simplecov)                                                                                                                                           | [Cobertura](https://github.com/dashingrocket/simplecov-cobertura)<br/>[LCOV](https://github.com/fortissimo1997/simplecov-lcov) | cobertura.xml<br/>lcov.info, lcov.dat, \*.lcov |
| C#                | [OpenCover](https://github.com/OpenCover/opencover)<br/>[DotCover CLI](https://www.jetbrains.com/dotcover/)                                                                                   | OpenCover<br/>DotCover-DetailedXML                                                                                             | opencover.xml<br/>dotcover.xml                 |
| Swift/Objective-C | XCode Coverage                                                                                                                                                                                | [Check here information about reports for this language](troubleshooting/swift-objectivec.md)                                  |                                                |

!!! tip
    If you are generating a report format that is not yet supported, try to generate a supported format using community projects such as [schrej/godacov](https://github.com/schrej/godacov) and [danielpalme/ReportGenerator](https://github.com/danielpalme/ReportGenerator), or alternatively contribute to our [codacy/coverage-parser](https://github.com/codacy/coverage-parser) project.

## 2. Uploading coverage reports to Codacy

1.  To set up coverage reporting you'll need a Project API token. You can find it in your repository settings 'Integrations' tab.

    Find and copy a _Project API Token_. You can find the token within a repository _Settings_ → _Integrations_ → _Project API_.

    !!! warning
        You should keep your API token well protected, as it grants owner permissions to your projects. If you use CircleCI or Travis CI, you should use your token as an environment variable. **Don't put your keys in your configuration files**, check your service settings on how to set environment variables.

    Set the _Project API Token_ in your terminal, replacing `%Project_Token%` with your token:

    ```bash
    export CODACY_PROJECT_TOKEN=%Project_Token%
    ```

    !!! hint
        If you would like to automate this process for multiple repositories you should [authenticate using an Account API Token](advanced/authenticating-using-an-account-api-token.md) instead.

1.  To upload coverage to a self-hosted installation of Codacy you also need to set your installation URL, replacing `%Codacy_instance_URL%` with your URL:

    ```bash
    export CODACY_API_BASE_URL=%Codacy_instance_URL%:16006
    ```

1.  The easiest way to get starting is by using the self-contained script that downloads and runs the reporter:

    ```bash
    bash <(curl -Ls https://coverage.codacy.com/get.sh)
    ```

    The reporter assumes the coverage reports filename follow the name convention. Otherwise, you must define the report's location with the flag `--coverage-reports`.

## Extra information

### Choose a specific version

The script uses by default the latest version.

If you want to specify a coverage reporter version, use `CODACY_REPORTER_VERSION` environment variable.

You can find all versions in the [Releases tab](https://github.com/codacy/codacy-coverage-reporter/releases).

### Manual Installation

Using CircleCI? Check out the [codacy/coverage-reporter orb](advanced/installation-methods.md#circleci-orb).

Using GitHub Actions? Check out the [codacy/coverage-coverage-reporter action](https://github.com/codacy/codacy-coverage-reporter-action#codacy-coverage-reporter-action).

If the automated script does not cover your use case, [check the manual installation methods](advanced/installation-methods.md#manually-downloading-the-native-binary).

### Unsupported Languages

If your language is not in the list of supported languages, you can still send coverage to Codacy. You can do it by providing the correct `--language` name and then add the `--force-language` flag.

### Submitting coverage for unsupported languages or tools

If your language or build tool isn't supported yet, you can send the coverage data directly through the Codacy API. You can check the endpoint in the [API documentation](https://api.codacy.com/swagger#savecoverage) and an example of the JSON payload below.

```json
{
  "total": 23,
  "fileReports": [
    {
      "filename": "src/Codacy/Coverage/Parser/CloverParser.php",
      "total": 54,
      "coverage": {
        "3": 3,
        "5": 0,
        "7": 1
      }
    }
  ]
}
```

!!! note
    In case the token was retrieved from the Repository integrations tab, the header should be `project-token`. If it is an account token, the header should be `api-token` and you must call [this API method](https://api.codacy.com/swagger#savecoveragewithprojectname) instead.

Also, note all _coverable_ lines should be present on the "coverage" variable of the JSON payload. In the example, you can see that "5": 0, meaning that line 5 is not covered.

### Commit SHA hash detection

Codacy automatically detects a commit SHA hash from CI workflows, the git repository or command line arguments. See [all supported environments](troubleshooting/commit-detection.md).

### Multiple coverage reports for the same language

You can upload multiple reports if your test suite is split in different modules or ran in parallel. See [how to upload multiple coverage reports](advanced/multiple-reports.md).
