---
description: Instructions on how to use the Codacy Coverage Reporter to upload coverage data in more advanced scenarios.
---

# Uploading coverage in advanced scenarios

The following sections include instructions on how to use the Codacy Coverage Reporter to upload coverage data in more advanced scenarios.

## Uploading multiple coverage reports for the same language {: id="multiple-reports"}

If your test suite is split in different modules or runs in parallel, you must upload multiple coverage reports for the same language either at once or in sequence.

!!! tip
    Alternatively, it might also be possible to merge the coverage reports before uploading them to Codacy, since most coverage tools support merging or aggregating the coverage data. For example, use the [merge mojo for JaCoCo](http://www.eclemma.org/jacoco/trunk/doc/merge-mojo.html).

### Uploading all reports at once {: id="multiple-reports-once"}

Upload multiple partial coverage reports with a single command by specifying each report with the flag `-r`. For example:

```bash
bash <(curl -Ls https://coverage.codacy.com/get.sh) report \
    -l Java -r report1.xml -r report2.xml -r report3.xml
```

You can also upload all your reports dynamically using the command `find`. For example:

```bash
bash <(curl -Ls https://coverage.codacy.com/get.sh) report \
    -l Java $(find . -name 'jacoco*.xml' -printf '-r %p ')
```

### Uploading reports in sequence {: id="multiple-reports-sequence"}

Upload multiple partial coverage reports in sequence:

1.  Upload each report separately with the flag `--partial`.

    If you're sending reports for a language with the flag `--partial`, you must use the flag in all reports for that language to ensure the correct calculation of the coverage.

1.  Notify Codacy with the `final` command after uploading all reports.

For example:

```bash
bash <(curl -Ls https://coverage.codacy.com/get.sh) report \
    --partial -l Java -r report1.xml
bash <(curl -Ls https://coverage.codacy.com/get.sh) report \
    --partial -l Java -r report2.xml
bash <(curl -Ls https://coverage.codacy.com/get.sh) final
```

## Uploading the same coverage report for multiple languages {: id="multiple-languages"}

If your test suite generates a single coverage report for more than one language, you must upload the same coverage report for each language.

To do this, upload the same report multiple times, specifying each different language with the flag `-l`. For example:

```bash
bash <(curl -Ls https://coverage.codacy.com/get.sh) report \
    -l Javascript -r report.xml
bash <(curl -Ls https://coverage.codacy.com/get.sh) report \
    -l TypeScript -r report.xml
```

## Uploading coverage for Golang {: id="golang"}

Codacy can't automatically detect Golang coverage report files because they don't have specific file names.

If you're uploading a Golang coverage report, you must also specify the report type:

```bash
bash <(curl -Ls https://coverage.codacy.com/get.sh) report \
    --force-coverage-parser go -r <coverage report file name>
```

## Uploading coverage for unsupported languages {: id="unsupported-languages"}

If your language isn't in the list of supported languages, you can still send coverage to Codacy.

To do this, provide the correct language with the flag `-l`, together with `--force-language`. For example:

```bash
bash <(curl -Ls https://coverage.codacy.com/get.sh) report \
  -l Kotlin --force-language -r <coverage report file name>
```

See the [list of languages](https://github.com/codacy/codacy-plugins-api/blob/master/src/main/scala/com/codacy/plugins/api/languages/Language.scala#L43) that you can specify using the flag `-l`.
