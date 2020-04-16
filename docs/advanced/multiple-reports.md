# Multiple coverage reports for the same language

In order to send multiple reports for the same language, you need to upload each report separately with the flag `--partial` and then notify Codacy, after all reports were sent, with the `final` command.

**_Example_**

1. `codacy-coverage-reporter report -l Java -r report1.xml --partial`
1. `codacy-coverage-reporter report -l Java -r report2.xml --partial`
1. `codacy-coverage-reporter final`

**_Using the script_**

1. `bash <(curl -Ls https://coverage.codacy.com/get.sh) report -l Java -r report1.xml --partial`
1. `bash <(curl -Ls https://coverage.codacy.com/get.sh) report -l Java -r report2.xml --partial`
1. `bash <(curl -Ls https://coverage.codacy.com/get.sh) final`

If you are sending reports with the partial flag for a certain language you should use it in all reports for that language to ensure the correct calculation of the coverage.

It might also be possible to merge the reports before uploading them to Codacy, since most coverage tools support merge/aggregation, example: http://www.eclemma.org/jacoco/trunk/doc/merge-mojo.html.
