---
description: Instructions or workarounds to overcome common issues while using Codacy Coverage Reporter.
---

# Troubleshooting common issues

The sections below provide instructions or workarounds to overcome common issues while using Codacy Coverage Reporter.

## Can't guess any report due to no matching

Codacy Coverage Reporter automatically searches for coverage reports matching the [file name conventions for supported formats](index.md#generating-coverage).

However, if Codacy Coverage Reporter does not find your coverage report, you can explicitly define the report file name with the flag `-r`. For example:

```bash
bash <(curl -Ls https://coverage.codacy.com/get.sh) report -r <coverage report file>
```

## Report generated an empty result while uploading C# coverage data {: id="detailedxml"}

If you're using dotCover to generate coverage reports for your C# projects, you must use the dotCover detailedXML report format as follows:

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

If you're experiencing segmentation faults when uploading the coverage results due to [oracle/graal#624](https://github.com/oracle/graal/issues/624), execute the following command before running the reporter, as a workaround:

```sh
echo "$(dig +short api.codacy.com | tail -n1) api.codacy.com" >> /etc/hosts
```

## coverage-xml/index.xml generated an empty result

If you're using PHPUnit version 5 or above to generate your coverage report, you must output the report using the Clover format. Codacy Coverage Reporter supports the PHPUnit XML format only for versions 4 and older.

To change the output format replace the flag `--coverage-xml <dir>` with `--coverage-clover <file>` when executing `phpunit`.

See [PHPUnit command-line documentation](https://phpunit.readthedocs.io/en/latest/textui.html) for more information.
