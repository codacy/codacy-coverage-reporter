# Troubleshooting common issues

## Unsupported languages

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

### Swift and Objective-C support {: id="swift-objectivec-support"}

To use Swift and Objective-C with Xcode coverage reports, you should use [Slather](https://github.com/SlatherOrg/slather) to convert Xcode report into Cobertura format.

This can be achieved by running these commands on the CI:

```bash
gem install slather
slather coverage -x --output-directory <report-output-dir> --scheme <project-name> <project-name>.xcodeproj
```

This will generate a `cobertura.xml` inside `<report-output-dir>` folder.

After this, call our script:

```bash
bash <(curl -Ls https://coverage.codacy.com/get.sh)
```

## Can't guess any report due to no matching

  Codacy Coverage Reporter automatically searches for coverage reports that match [these file name conventions](index.md#generating-coverage).
  
  However, if Codacy Coverage Reporter does not find your coverage report, you must define the report file name explicitly with the flag `--coverage-reports`. For example:

  ```bash
  bash <(curl -Ls https://coverage.codacy.com/get.sh) report \
      --coverage-reports <my report>
  ```

## JsonParseException while uploading C# coverage data

If you're using dotCover to generate coverage reports for your C# projects, you should [exclude xUnit files](https://www.jetbrains.com/help/dotcover/Running_Coverage_Analysis_from_the_Command_LIne.html#filters_cmd) from the coverage analysis as follows:

```bash
dotCover.exe cover ... /Filters=-:xunit*
```

By default, dotCover includes xUnit files in the coverage analysis and this results in larger coverage reports. This filter helps ensure that the resulting coverage data does not exceed the size limit accepted by the Codacy API when uploading the results.

## SubstrateSegfaultHandler caught signal 11

If you are experiencing segmentation faults uploading the coverage (due to [oracle/graal#624](https://github.com/oracle/graal/issues/624)), do this before running the reporter, as a workaround:

```sh
echo "$(dig +short api.codacy.com | tail -n1) api.codacy.com" >> /etc/hosts
```

## coverage-xml/index.xml generated an empty result

If you are using PHPUnit version 5 or above to generate your coverage report, you must output the report using the clover format. The codacy-coverage-reporter supports the PHPUnit xml format only for versions equal or lower than 4.
You can change the output format by replacing the `--coverage-xml <dir>` flag by `--coverage-clover <file>`.

For more information on the PHPUnit command line check [here](https://phpunit.readthedocs.io/en/9.1/textui.html).
