---
description: Instructions or workarounds to overcome common issues while using Codacy Coverage Reporter.
---

# Troubleshooting common issues

The sections below provide instructions or workarounds to overcome common issues while using Codacy Coverage Reporter.

## Submitting coverage for unsupported languages

If your language is not in the list of supported languages, you can still send coverage to Codacy. You can do it by providing the correct language name with the flag `-l`, together with `--force-language`. For example:

```bash
bash <(curl -Ls https://coverage.codacy.com/get.sh) report
  -l Kotlin --force-language
```

See the [list of languages](https://github.com/codacy/codacy-plugins-api/blob/master/src/main/scala/com/codacy/plugins/api/languages/Language.scala#L43) that you can specify using the flag `-l`.

## Submitting coverage from unsupported report formats {: id="unsupported-report-formats"}

If you are generating a report format that Codacy does not yet support, try using the community projects below or contribute to our [codacy/coverage-parser](https://github.com/codacy/coverage-parser) project:

-   [danielpalme/ReportGenerator](https://github.com/danielpalme/ReportGenerator): convert between different report formats 
-   [dariodf/lcov_ex](https://github.com/dariodf/lcov_ex): generate LCOV reports for Elixir projects
-   [t-yuki/gocover-cobertura](https://github.com/t-yuki/gocover-cobertura): generate Cobertura reports from [Go cover](https://golang.org/pkg/cmd/cover/) reports
-   [chrisgit/sfdx-plugins_apex_coverage_report](https://github.com/chrisgit/sfdx-plugins_apex_coverage_report): generate LCOV or Cobertura reports from [Apex](https://help.salesforce.com/articleView?id=sf.code_apex_dev_guide_tools.htm&type=5) test coverage data

As a workaround, you can also send the coverage data directly by calling the Codacy API endpoint [saveCoverage](https://api.codacy.com/swagger#savecoverage) (when using a project API Token)
or [saveCoverageWithAccountToken](https://api.codacy.com/swagger#savecoveragewithaccounttoken) (when using an account API Token).

The following is an example of the JSON payload:

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

Note that all "coverable" lines should be present on the `coverage` node of the JSON payload. In the example you can see `"5": 0`, meaning that line 5 is not covered.

## Can't guess any report due to no matching

Codacy Coverage Reporter automatically searches for coverage reports matching the [file name conventions for supported formats](index.md#generating-coverage).

However, if Codacy Coverage Reporter does not find your coverage report, you can explicitly define the report file name with the flag `-r`. For example:

```bash
bash <(curl -Ls https://coverage.codacy.com/get.sh) report \
    -r <my report>
```

## Report generated an empty result while uploading C# coverage data {: id="detailedxml"}

If you are using dotCover to generate coverage reports for your C# projects, you must use the dotCover detailedXML report format as follows:

```bash
dotCover.exe cover ... --reportType=DetailedXml
```

## JsonParseException while uploading C# coverage data

If you are using dotCover to generate coverage reports for your C# projects, you should [exclude xUnit files](https://www.jetbrains.com/help/dotcover/Running_Coverage_Analysis_from_the_Command_LIne.html#filters_cmd) from the coverage analysis as follows:

```bash
dotCover.exe cover ... /Filters=-:xunit*
```

By default, dotCover includes xUnit files in the coverage analysis and this results in larger coverage reports. This filter helps ensure that the resulting coverage data does not exceed the size limit accepted by the Codacy API when uploading the results.

## MalformedInputException while parsing report

If you get a `java.nio.charset.MalformedInputException` when running the Codacy Coverage Reporter it means that the coverage report includes an unsupported character, perhaps on one of your source code file names.

For maximum compatibility of your coverage reports with the Codacy Coverage Reporter, make sure that your coverage reports use UTF-8 encoding or remove any special characters from the reports.

## SubstrateSegfaultHandler caught signal 11

If you are experiencing segmentation faults when uploading the coverage results due to [oracle/graal#624](https://github.com/oracle/graal/issues/624), execute the following command before running the reporter, as a workaround:

```sh
echo "$(dig +short api.codacy.com | tail -n1) api.codacy.com" >> /etc/hosts
```

## coverage-xml/index.xml generated an empty result

If you are using PHPUnit version 5 or above to generate your coverage report, you must output the report using the Clover format. Codacy Coverage Reporter supports the PHPUnit XML format only for versions 4 and older.

To change the output format replace the flag `--coverage-xml <dir>` with `--coverage-clover <file>` when executing `phpunit`.

See [PHPUnit command-line documentation](https://phpunit.readthedocs.io/en/latest/textui.html) for more information.
