# Common issues

## `JsonParseException` while uploading C# coverage data

If you're using dotCover to generate coverage reports for your C# projects, you should [exclude xUnit files](https://www.jetbrains.com/help/dotcover/Running_Coverage_Analysis_from_the_Command_LIne.html#filters_cmd) from the coverage analysis as follows:

```bash
dotCover.exe cover ... /Filters=-:xunit*
```

By default, dotCover includes xUnit files in the coverage analysis and this results in larger coverage reports. This filter helps ensure that the resulting coverage data does not exceed the size limit accepted by the Codacy API when uploading the results.

## `SubstrateSegfaultHandler caught signal 11`

If you are experiencing segmentation faults uploading the coverage (due to [oracle/graal#624](https://github.com/oracle/graal/issues/624)), do this before running the reporter, as a workaround:

```sh
echo "$(dig +short api.codacy.com | tail -n1) api.codacy.com" >> /etc/hosts
```

## `coverage-xml/index.xml generated an empty result`

If you are using PHPUnit version 5 or above to generate your coverage report, you must output the report using the clover format. The codacy-coverage-reporter supports the PHPUnit xml format only for versions equal or lower than 4.
You can change the output format by replacing the `--coverage-xml <dir>` flag by `--coverage-clover <file>`.

For more information on the PHPUnit command line check [here](https://phpunit.readthedocs.io/en/9.1/textui.html).
