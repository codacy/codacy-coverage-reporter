# Adding coverage to your repository

Before setting up Codacy to display code coverage metrics for your repository you must have tests and use tools to generate coverage reports for the languages in your repositories. If you need help on getting started see [how to generate coverage reports](generating-coverage-reports.md).

Codacy supports the following coverage report formats:

| Report formats                                              | Report file names            |
| ----------------------------------------------------------- | ---------------------------- |
| [Clover](https://openclover.org/)                           | clover.xml                   |
| [Cobertura](http://cobertura.github.io/cobertura/)          | cobertura.xml                |
| [dotCover](https://www.jetbrains.com/dotcover/) XML         | dotcover.xml                 |
| [JaCoCo](https://www.jacoco.org/)                           | jacoco\*.xml                 |
| [LCOV](http://ltp.sourceforge.net/coverage/lcov/readme.php) | lcov.info, lcov.dat, \*.lcov |
| [OpenCover](https://github.com/OpenCover/opencover)         | opencover.xml                |
| [PHPUnit](https://phpunit.de/) XML (version &lt;= 4)        | coverage-xml/index.xml       |

!!! tip
    If you are generating a report format that Codacy does not yet support, try using community projects such as [schrej/godacov](https://github.com/schrej/godacov) and [danielpalme/ReportGenerator](https://github.com/danielpalme/ReportGenerator), or alternatively contribute to our [codacy/coverage-parser](https://github.com/codacy/coverage-parser) project.

After having coverage reports set up for your repository, you must use Codacy Coverage Reporter to convert the reports to smaller JSON files and upload these files to Codacy:

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

2.  **If you are using Codacy Self-hosted** set the following environment variable to specify your Codacy instance URL:

    ```bash
    export CODACY_API_BASE_URL=<your Codacy instance URL>:16006
    ```

3.  Run Codacy Coverage Reporter to upload the coverage results to Codacy.

    The recommended way to do this is using a self-contained script that automatically downloads and runs the most recent version of Codacy Coverage Reporter:

    ```bash
    bash <(curl -Ls https://coverage.codacy.com/get.sh) report
    ```

    See [alternative ways of running Codacy Coverage Reporter](alternative-ways-of-running-coverage-reporter.md) for other ways of running Codacy Coverage Reporter, such as when using Circle CI or GitHub actions, or to install the binary manually.

See the sections below for more advanced functionality.

## Uploading multiple coverage reports for the same language {: id="multiple-reports"}

If your test suite is split in different modules or runs in parallel, you will need to upload multiple coverage reports for the same language.

To do this, upload each separate report with the flags `--partial`, `--language` (to specify the language), and `--coverge-reports` (to specify each partial report). Then, after all reports were uploaded, notify Codacy with the `final` command. For example:

```bash
bash <(curl -Ls https://coverage.codacy.com/get.sh) report \
    --language Java --coverage-reports report1.xml --partial
bash <(curl -Ls https://coverage.codacy.com/get.sh) report \
    --language Java --coverage-reports report2.xml --partial
bash <(curl -Ls https://coverage.codacy.com/get.sh) final
```

!!! important
    If you are sending reports for a language with the flag `--partial`, you should use the flag in all reports for that language to ensure the correct calculation of the coverage.

!!! tip
    It might also be possible to merge the reports before uploading them to Codacy, since most coverage tools support merge/aggregation. For example, <http://www.eclemma.org/jacoco/trunk/doc/merge-mojo.html>.

## Commit SHA hash detection {: id="commit-detection"}

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
