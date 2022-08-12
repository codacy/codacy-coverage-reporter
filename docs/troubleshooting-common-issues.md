---
description: Instructions or workarounds to overcome common issues while using Codacy Coverage Reporter.
---

# Troubleshooting common issues

The sections below provide instructions or workarounds to overcome common issues while using Codacy Coverage Reporter.

## No coverage data is visible on the Codacy UI {: id="no-coverage-visible"}

If the Codacy Coverage Reporter correctly uploaded your coverage report but the coverage data doesn't show up on the Codacy UI, please validate the following:

-   Make sure that the file paths included in your coverage reports are relative to the root directory of your repository. For example, `src/index.js`.
-   Verify that the Codacy Coverage Reporter is uploading the coverage data for the [correct commit in the correct branch](#commit-detection).
-   For pull requests, make sure that you have uploaded the coverage data for both:

    -   The commit that is the common ancestor of the pull request branch and the target branch
    -   The last commit in the pull request branch

    The following diagram highlights the commits that must have received coverage data for Codacy to display coverage information on a pull request:

    ![Commits that must have coverage data](images/coverage-pr-commits.png)

## Commit SHA-1 hash detection {: id="commit-detection"}

The Codacy Coverage Reporter automatically detects the SHA-1 hash of the current commit to associate with the coverage data when you're using one of the following CI/CD platforms:

-   Appveyor
-   AWS CodeBuild
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

If the Codacy Coverage Reporter fails to detect the current commit from the CI workflow context, it will use the current commit from the local Git repository instead.

However, you can also force using a specific commit SHA-1 hash with the flag `--commit-uuid`. For example:

```bash
bash <(curl -Ls https://coverage.codacy.com/get.sh) report \
    -r report.xml \
    --commit-uuid cd4d000083a744cf1617d46af4ec108b79e06bed
```

## Can't guess any report due to no matching

Codacy Coverage Reporter automatically searches for coverage reports matching the [file name conventions for supported formats](index.md#generating-coverage).

However, if Codacy Coverage Reporter does not find your coverage report, you can explicitly define the report file name with the flag `-r`. For example:

```bash
bash <(curl -Ls https://coverage.codacy.com/get.sh) report -r <coverage report file name>
```

## Report generated an empty result while uploading C# coverage data {: id="detailedxml"}

If you're using dotCover to generate coverage reports for your C# projects, you must use the dotCover detailedXML report format as follows:

```bash
dotCover.exe cover ... --reportType=DetailedXml
```

## JsonParseException while uploading coverage data

If you get a `com.fasterxml.jackson.core.JsonParseException` error while uploading your coverage data to Codacy it means that your coverage report is too big and that Codacy Coverage Reporter hit a limit of 10 MB when uploading the coverage data to Codacy.

There are some ways you can solve this:

-   Split your coverage reports into smaller files and [upload them to Codacy one at a time](../uploading-coverage-in-advanced-scenarios/#multiple-reports).

-   **If you're using dotCover to generate coverage reports for your C# projects**, you should [exclude xUnit files](https://www.jetbrains.com/help/dotcover/Running_Coverage_Analysis_from_the_Command_LIne.html#filters_cmd) from the coverage analysis as follows:

    ```bash
    dotCover.exe cover ... /Filters=-:xunit*
    ```

    By default, dotCover includes xUnit files in the coverage analysis and this results in larger coverage reports. This filter helps ensure that the resulting coverage data does not exceed the size limit accepted by the Codacy API when uploading the results.

## Connect timed out while uploading coverage data

If you get a `Error doing a post to ... connect timed out` error while uploading your coverage data to Codacy it means that the Codacy Coverage Reporter is timing out while connecting to the Codacy API. This typically happens if you're uploading coverage data for larger repositories.

To increase the default timeout while connecting to the Codacy API, use the flag `--http-timeout` to set a value larger than 10000 miliseconds. For example, to set the timeout to 30 seconds:

```bash
bash <(curl -Ls https://coverage.codacy.com/get.sh) report \
    -r report.xml \
    --http-timeout 30000
```

## MalformedInputException while parsing report

If you get a `java.nio.charset.MalformedInputException` when running the Codacy Coverage Reporter it means that the coverage report includes a character that is not encoded in UTF-8. The invalid character can belong to the file name of one of your source code files, or even a class or method name.

For maximum compatibility of your coverage reports with the Codacy Coverage Reporter, make sure that your coverage reports use UTF-8 encoding and that they only include UTF-8 characters.

## SubstrateSegfaultHandler caught signal 11

If you're experiencing segmentation faults when uploading the coverage results due to [oracle/graal#624](https://github.com/oracle/graal/issues/624), execute the following command before running the reporter, as a workaround:

```sh
echo "$(dig +short api.codacy.com | tail -n1) api.codacy.com" >> /etc/hosts
```

## coverage-xml/index.xml generated an empty result

If you're using PHPUnit version 5 or above to generate your coverage report, you must output the report using the Clover format. Codacy Coverage Reporter supports the PHPUnit XML format only for versions 4 and older.

To change the output format replace the flag `--coverage-xml <dir>` with `--coverage-clover <file>` when executing `phpunit`.

See [PHPUnit command-line documentation](https://phpunit.readthedocs.io/en/9.5/textui.html) for more information.

## Can't validate checksum {: id="checksum"}

Starting on version [13.0.0](https://github.com/codacy/codacy-coverage-reporter/releases/tag/13.0.0) the `get.sh` script automatically validates the checksum of the downloaded Codacy Coverage Reporter binary. This requires having either the `sha512sum` or `shasum` command on the operating system where you're running the script.

If you're getting this error while uploading your coverage data to Codacy, install the correct version of `sha512sum` or `shasum` for the operating system that you're using.

You can also skip validating the checksum of the binary by defining the following environment variable, however, Codacy doesn't recommend this:

```bash
export CODACY_REPORTER_SKIP_CHECKSUM=true
```

## Error status

### Commit not found

Codacy doesn't know about the commit reported with the coverage, either because:

#### 1. Codacy hasn't received/processed the hook for that commit from the Git provider

1.1. Wait a few more minutes until Codacy "knows about the commit" and the status will update automatically

1.2. If it takes too long for Codacy to detect the commit, the hook call from the Git provider may have been lost. Contact Support to sync the commits with the Git provider

#### 2. The commit UUID send while uploading coverage is wrong

2. Make sure that the Codacy Coverage Reporter is [detecting the correct commit UUID](#commit-detection) for the coverage data it is uploading

### Branch not enabled

The commit reported with the coverage doesn't belong to any branch that Codacy is analyzing:

#### 1. Coverage was uploaded for a commit in a branch that isn't explicitly enabled on Codacy nor belongs to a pull request branch targeting an enabled branch

1.1. Make sure that the [branch or target branch for pull requests is enabled on Codacy](../repositories-configure/managing-branches/)

1.2. Make sure that the Codacy Coverage Reporter is uploading the coverage data for the correct branch and [detecting the correct commit UUID](#commit-detection)

#### 2. Coverage was uploaded for a commit that no longer belongs to any branch on the Git repository, for example after a rebase or squash-merge

2. This is a false positive error status since it is expected depending on the Git workflows of the developers, and in my opinion should be considered a bug <!--TODO Rewrite-->

### Commit not analyzed

Due to technical limitations, Codacy only reports coverage for a commit after performing static code analysis on that commit:

#### 1. Codacy hasn't finished analyzing the commit yet

1. Wait until Codacy completes the static code analysis for the commit and the status will update automatically

#### 2. Codacy didn't analyze the commit on a private repository because the commit author isn't a member of the Codacy organization

2. Make sure that all commit authors are [added as members of the organization](../organizations/managing-people/#adding-people)

#### 3. Codacy skipped analyzing the commit because there are more recent commits

3. Upload coverage data for the most recent commit in the branch

#### 4. Codacy ran into an error while performing the static code analysis of the commit

4.1. Solve the underlying issue causing the analysis to fail (for example, fix the SSH key)

4.2. Contact Support asking for help

#### 5. The setting Run analysis on your build server is on, but the tools running locally aren't reporting results back to Codacy

5. Make sure that the local tools run successfully and that they report the results back to Codacy to complete the analysis

### Pending

#### 1. Coverage was [uploaded with the "partial" flag](../coverage-reporter/#multiple-reports) but Codacy didn't receive the "final" notification

1.1. Make sure that after uploading all partial reports you [send the "final notification"](../coverage-reporter/#multiple-reports)

1.2. Alternatively, send all partial reports by [calling the Codacy Coverage Reporter only once](../coverage-reporter/#multiple-reports)

#### 2. Coverage uploaded only includes information for files that are [ignored on Codacy](../repositories-configure/ignoring-files/)

2. This error status doesn't map well to the scenario causing it and Codacy could instead either report this status as Processed or with a new status to let users know why it won't display their coverage data, and in my opinion this scenario should be considered a bug <!--TODO Rewrite-->

##### 3. An empty coverage data set (`"total": 0`) was uploaded using the Codacy API

3. This error status doesn't map well to the scenario causing it and Codacy could instead either report this status as Processed or with a new status to let users know why it won't display their coverage data, and in my opinion this scenario should be considered a bug <!--TODO Rewrite-->
