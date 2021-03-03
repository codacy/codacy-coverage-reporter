# Adding coverage to your repository

Before setting up Codacy to display code coverage metrics for your repository you must have tests and use tools to generate coverage reports for the languages in your repositories. If you need help on getting started see [how to generate coverage reports](generating-coverage-reports.md).

Codacy supports the following coverage report formats:

| Report format                                                        | Report file name             |
| -------------------------------------------------------------------- | ---------------------------- |
| Clover                                                               | clover.xml                   |
| Cobertura                                                            | cobertura.xml                |
| dotCover [detailedXML](troubleshooting-common-issues.md#detailedxml) | dotcover.xml                 |
| JaCoCo                                                               | jacoco\*.xml                 |
| LCOV                                                                 | lcov.info, lcov.dat, \*.lcov |
| OpenCover                                                            | opencover.xml                |
| PHPUnit XML (version &lt;= 4)                                        | coverage-xml/index.xml       |

!!! note
    If you are generating a report format that Codacy does not yet support, see [submitting coverage from unsupported report formats](troubleshooting-common-issues.md#unsupported-report-formats).

After having coverage reports set up for your repository, you must use Codacy Coverage Reporter to convert the reports to smaller JSON files and upload these files to Codacy. The recommended way to do this is using a CI/CD platform that automatically runs tests, generates coverage, and uses Codacy Coverage Reporter to upload the coverage report information for every commit.

1.  Set up an API token to allow Codacy Coverage Reporter to authenticate on Codacy.
    {: id="authenticate"}

    Obtain the [project API Token](/repositories-configure/integrations/project-api/) from the page **Integrations** in your Codacy repository settings. Then, set the following environment variable to specify your project API Token:

    ```bash
    export CODACY_PROJECT_TOKEN=<your project API Token>
    ```

    !!! warning
        **Never write API Tokens on your configuration files** and keep your API Tokens well protected, as they grant owner permissions to your Codacy repositories.

        We recommend that you set the API Tokens as environment variables. Check the documentation of your CI/CD platform on how to do this.

1.  **If you are using Codacy Self-hosted** set the following environment variable to specify your Codacy instance URL:

    ```bash
    export CODACY_API_BASE_URL=<your Codacy instance URL>
    ```

1.  Run Codacy Coverage Reporter **on the root of the locally checked out branch of your Git repository**, specifying the relative path to the coverage report to upload. For example:

    ```bash
    bash <(curl -Ls https://coverage.codacy.com/get.sh) report -r report.xml
    ```

    !!! tip
        The recommended self-contained script automatically downloads and runs the most recent version of Codacy Coverage Reporter.

        See [alternative ways of running Codacy Coverage Reporter](alternative-ways-of-running-coverage-reporter.md) for other ways of running Codacy Coverage Reporter, such as by installing the binary manually or using a GitHub Action or CircleCI Orb.

1.  Optionally, [add a Codacy badge](https://docs.codacy.com/repositories/badges/) to the README of your repository to display the current code coverage.

See the sections below for more advanced functionality, or [check the troubleshooting page](troubleshooting-common-issues.md) if you found an issue during the setup process.

## Uploading multiple coverage reports for the same language {: id="multiple-reports"}

If your test suite is split on different modules or runs in parallel, you will need to upload multiple coverage reports for the same language:

1.  Upload each separate report with the following flags:

    - `--partial`
    - `-l <language>`
    - `-r <partial report>`

2.  After uploading all reports, notify Codacy with the `final` command.

For example:

```bash
bash <(curl -Ls https://coverage.codacy.com/get.sh) report \
    --partial -l Java -r report1.xml
bash <(curl -Ls https://coverage.codacy.com/get.sh) report \
    --partial -l Java -r report2.xml
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
    -   Azure Pipelines
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
