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

<table>
<thead>
<tr>
    <th>What causes the error?</th>
    <th>How to solve the error?</th>
</tr>
</thead>
<tbody>
<tr>
    <td>Codacy hasn't received/processed the hook for that commit from the Git provider</td>
    <td><ul>
        <li>Wait a few more minutes until Codacy "knows about the commit" and the status will update automatically</li>
        <li>If it takes too long for Codacy to detect the commit, the hook call from the Git provider may have been lost. Contact Support to sync the commits with the Git provider</li>
    </ul></td>
</tr>
<tr>
    <td>The commit SHA-1 hash sent while uploading coverage is wrong</td>
    <td><ul>
        <li>Make sure that the Codacy Coverage Reporter is <a href="#commit-detection">detecting the correct commit SHA-1 hash</a> for the coverage data it is uploading</li>
    </ul></td>
</tr>
</table>

### Branch not enabled

The commit reported with the coverage doesn't belong to any branch that Codacy is analyzing:

<table>
<thead>
<tr>
    <th>What causes the error?</th>
    <th>How to solve the error?</th>
</tr>
</thead>
<tbody>
<tr>
    <td>Coverage was uploaded for a commit in a branch that isn't explicitly enabled on Codacy nor belongs to a pull request branch targeting an enabled branch</td>
    <td><ul>
        <li>Make sure that the [branch or target branch for pull requests is enabled on Codacy](../repositories-configure/managing-branches/)</li>
        <li>Make sure that the Codacy Coverage Reporter is uploading the coverage data for the correct branch and [detecting the correct commit UUID](#commit-detection)</li>
    </ul></td>
</tr>
<tr>
    <td>Coverage was uploaded for a commit that no longer belongs to any branch on the Git repository, for example after a rebase or squash-merge</td>
    <td><ul>
        <li>This is a false positive error status since it is expected depending on the Git workflows of the developers, and in my opinion should be considered a bug <!--TODO Rewrite--></li>
    </ul></td>
</tr>
</table>

### Commit not analyzed

Due to technical limitations, Codacy only reports coverage for a commit after performing static code analysis on that commit:

<table>
<thead>
<tr>
    <th>What causes the error?</th>
    <th>How to solve the error?</th>
</tr>
</thead>
<tbody>
<tr>
    <td>Codacy hasn't finished analyzing the commit yet</td>
    <td><ul>
        <li>Wait until Codacy completes the static code analysis for the commit and the status will update automatically</li>
    </ul></td>
</tr>
<tr>
    <td>Codacy didn't analyze the commit on a private repository because the commit author isn't a member of the Codacy organization</td>
    <td><ul>
        <li>Make sure that all commit authors are [added as members of the organization](../organizations/managing-people/#adding-people)</li>
    </ul></td>
</tr>
<tr>
    <td>Codacy skipped analyzing the commit because there are more recent commits</td>
    <td><ul>
        <li>Upload coverage data for the most recent commit in the branch</li>
    </ul></td>
</tr>
<tr>
    <td>Codacy ran into an error while performing the static code analysis of the commit</td>
    <td><ul>
        <li>Solve the underlying issue causing the analysis to fail (for example, fix the SSH key)</li>
        <li>Contact Support asking for help</li>
    </ul></td>
</tr>
<tr>
    <td>The setting <strong>Run analysis on your build server</strong> is on, but the tools running locally aren't reporting results back to Codacy</td>
    <td><ul>
        <li>Make sure that the local tools run successfully and that they report the results back to Codacy to complete the analysis</li>
    </ul></td>
</tr>
</table>

### Pending

<table>
<thead>
<tr>
    <th>What causes the error?</th>
    <th>How to solve the error?</th>
</tr>
</thead>
<tbody>
<tr>
    <td>Coverage was [uploaded with the "partial" flag](../coverage-reporter/#multiple-reports) but Codacy didn't receive the "final" notification</td>
    <td><ul>
        <li>Make sure that after uploading all partial reports you [send the "final notification"](../coverage-reporter/#multiple-reports)</li>
        <li>Alternatively, send all partial reports by [calling the Codacy Coverage Reporter only once](../coverage-reporter/#multiple-reports)</li>
    </ul></td>
</tr>
<tr>
    <td>Coverage uploaded only includes information for files that are [ignored on Codacy](../repositories-configure/ignoring-files/)</td>
    <td><ul>
        <li>This error status doesn't map well to the scenario causing it and Codacy could instead either report this status as Processed or with a new status to let users know why it won't display their coverage data, and in my opinion this scenario should be considered a bug <!--TODO Rewrite--></li>
    </ul></td>
</tr>
<tr>
    <td>An empty coverage data set (`"total": 0`) was uploaded using the Codacy API</td>
    <td><ul>
        <li>This error status doesn't map well to the scenario causing it and Codacy could instead either report this status as Processed or with a new status to let users know why it won't display their coverage data, and in my opinion this scenario should be considered a bug <!--TODO Rewrite--></li>
    </ul></td>
</tr>
</table>
