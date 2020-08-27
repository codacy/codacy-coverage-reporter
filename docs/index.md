# Adding coverage to your repository

Code coverage is a metric used to describe the degree to which the source code of a program is tested. A program with high code coverage has been more thoroughly tested and has a lower chance of containing software bugs than a program with low code coverage.

For Codacy to display code coverage metrics and reports for your repository you must complete these main steps:

1.  **Generating coverage for your repository**

    You must have tests and use one or more tools to generate coverage reports for your repository.

1.  **Uploading coverage reports to Codacy**

    Use the tool Codacy Coverage Reporter to read the generated coverage reports and upload the information to Codacy.

The next sections include detailed instructions on how to complete each step to add coverage to your repository.

## 1. Generating coverage for your repository {: id="generating-coverage"}

There are many tools that you can use to generate coverage for your repositories. The following table contains coverage tools that generate reports in formats that Codacy supports:

| Language          | Example coverage tools                                                                                                                                                                        | Report formats                                                                                                                 | Report file names                               |
| ----------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------ | ----------------------------------------------- |
| C#                | [OpenCover](https://github.com/OpenCover/opencover)<br/>[dotCover CLI](https://www.jetbrains.com/help/dotcover/Running_Coverage_Analysis_from_the_Command_LIne.html)                          | OpenCover<br/>dotCover-DetailedXML                                                                                             | opencover.xml<br/>dotcover.xml                  |
| Java              | [JaCoCo](http://eclemma.org/jacoco/)<br/>[Cobertura](http://cobertura.github.io/cobertura/)                                                                                                   | JaCoCo<br/>Cobertura                                                                                                           | jacoco\*.xml<br/>cobertura.xml                  |
| JavaScript        | [Istanbul](https://github.com/gotwarlost/istanbul)<br/>[Poncho](https://github.com/deepsweet/poncho)<br/>[Mocha](http://mochajs.org/) + [Blanket.js](https://github.com/alex-seville/blanket) | LCOV                                                                                                                           | lcov.info, lcov.dat, \*.lcov                    |
| PHP               | [PHPUnit](https://phpunit.readthedocs.io/en/9.3/code-coverage-analysis.html)                                                                                                                  | PHPUnit XML (version &lt;= 4)<br/>[Clover](https://confluence.atlassian.com/clover/using-clover-for-php-420973033.html)        | coverage-xml/index.xml<br/>clover.xml           |
| Python            | [Coverage.py](https://coverage.readthedocs.io/en/coverage-5.0.3/)                                                                                                                             | Cobertura                                                                                                                      | cobertura.xml                                   |
| Ruby              | [SimpleCov](https://github.com/colszowka/simplecov)                                                                                                                                           | [Cobertura](https://github.com/dashingrocket/simplecov-cobertura)<br/>[LCOV](https://github.com/fortissimo1997/simplecov-lcov) | cobertura.xml<br/>lcov.info, lcov.dat, \*.lcov  |
| Scala             | [sbt-jacoco](https://www.scala-sbt.org/sbt-jacoco/)<br/>[scoverage](http://scoverage.org/)                                                                                                    | JaCoCo<br/>Cobertura                                                                                                           | jacoco\*.xml<br/>cobertura.xml                  |
| Swift/Objective-C | [Xcode](https://developer.apple.com/library/archive/documentation/DeveloperTools/Conceptual/testing_with_xcode/chapters/07-code_coverage.html) Code Coverage                                  | [See how to generate coverage reports with Xcode](troubleshooting-common-issues.md#swift-objectivec-support)                   |                                                 |

!!! tip
    If you are generating a report format that Codacy does not yet support, try using community projects such as [schrej/godacov](https://github.com/schrej/godacov) and [danielpalme/ReportGenerator](https://github.com/danielpalme/ReportGenerator), or alternatively contribute to our [codacy/coverage-parser](https://github.com/codacy/coverage-parser) project.

## 2. Uploading coverage reports to Codacy

After having coverage reports set up for your repository, you must use Codacy Coverage Reporter to convert the reports to a smaller JSON file and upload this file to Codacy:

1.  You must set up an API token to allow Codacy Coverage Reporter to authenticate on Codacy.

    -   **If you're setting up coverage for one repository**, obtain the [project API Token](/repositories-configure/integrations/project-api/) from the page **Integrations** in your Codacy repository settings.
    
        Then, set the following environment variable to specify your project API Token:

        ```bash
        export CODACY_PROJECT_TOKEN=<your project API Token>
        ```

    -   **If you're setting up and automating coverage for multiple repositories**, obtain an [account API Token](https://docs.codacy.com/related-tools/api-tokens/) from the page **Access management** in your Codacy account settings.

        Then, set the following environment variables to specify the account API Token, the username associated with the account API token, and the repository for which you're uploading the coverage information:

        ```bash
        export CODACY_API_TOKEN=<your account API Token>
        export CODACY_USERNAME=<your account username>
        export CODACY_PROJECT_NAME=<name of your repository>
        ```

    !!! warning
        **Never write API Tokens on your configuration files** and keep your API Tokens well protected, as they grant owner permissions to your projects.
        
        We recommend that you set the API Tokens as environment variables. Check the documentation of your CI/CD platform on how to do this.

1.  **If you are using Codacy Self-hosted** set the following environment variable to specify your Codacy instance URL:

    ```bash
    export CODACY_API_BASE_URL=<your Codacy instance URL>:16006
    ```

1.  Run Codacy Coverage Reporter to upload the coverage results to Codacy.

    The recommended way to do this is using a self-contained script that automatically downloads and runs the most recent version of Codacy Coverage Reporter:

    ```bash
    bash <(curl -Ls https://coverage.codacy.com/get.sh) report
    ```

    See [alternative ways of running Codacy Coverage Reporter](alternative-ways-of-running-coverage-reporter.md) for other ways of running Codacy Coverage Reporter, such as when using Circle CI or GitHub actions, or to install the binary manually.

See the sections below for more advanced functionality.

### Uploading multiple coverage reports for the same language {: id="multiple-reports"}

If your test suite is split in different modules or runs in parallel, you will need to upload multiple coverage reports for the same language.

To do this, upload each separate report with the flag `--partial` and notify Codacy with the `final` command after all reports were uploaded. For example:

```bash
bash <(curl -Ls https://coverage.codacy.com/get.sh) report \
    --language Java --coverage-reports report1.xml --partial
bash <(curl -Ls https://coverage.codacy.com/get.sh) report \
    --language Java --coverage-reports report2.xml --partial
bash <(curl -Ls https://coverage.codacy.com/get.sh) report final
```

!!! important
    If you are sending reports for a language with the flag `--partial`, you should use the flag in all reports for that language to ensure the correct calculation of the coverage.

!!! tip
    It might also be possible to merge the reports before uploading them to Codacy, since most coverage tools support merge/aggregation. For example, <http://www.eclemma.org/jacoco/trunk/doc/merge-mojo.html>.

### Commit SHA hash detection {: id="commit-detection"}

Codacy Coverage Reporter automatically detects the commit SHA hash to associate with the coverage data from several sources, in the following order:

1.  **CI workflow context**

    Codacy Coverage Reporter supports the following CI/CD platforms:

    -   Appveyor
    -   Bitrise
    -   Buildkite
    -   Circle CI
    -   Codefresh
    -   Codeship
    -   Docker
    -   GitLab
    -   Greenhouse CI
    -   Heroku CI
    -   Jenkins
    -   Magnum CI
    -   Semaphore CI
    -   Shippable CI
    -   Solano CI
    -   TeamCity CI
    -   Travis CI
    -   Wercker CI

2.  **Git repository directory**

    If Codacy Coverage Reporter finds a Git repository directory it will use the current commit.

However, you can also force using a specific commit SHA hash with the flag `--commit-uuid`. For example:

```bash
bash <(curl -Ls https://coverage.codacy.com/get.sh) report \
    --commit-uuid cd4d000083a744cf1617d46af4ec108b79e06bed
```
