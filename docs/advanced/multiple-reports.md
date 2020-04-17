# Multiple coverage reports for the same language

In order to send multiple reports for the same language, you need to upload each report separately with the flag `--partial` and then notify Codacy, after all reports were sent, with the `final` command.

Example:

```bash
codacy-coverage-reporter report -l Java -r report1.xml --partial
codacy-coverage-reporter report -l Java -r report2.xml --partial
codacy-coverage-reporter final
```

Using the script:

```bash
bash <(curl -Ls https://coverage.codacy.com/get.sh) report -l Java -r report1.xml --partial
bash <(curl -Ls https://coverage.codacy.com/get.sh) report -l Java -r report2.xml --partial
bash <(curl -Ls https://coverage.codacy.com/get.sh) final
```

If you are sending reports with the partial flag for a certain language you should use it in all reports for that language to ensure the correct calculation of the coverage.

It might also be possible to merge the reports before uploading them to Codacy, since most coverage tools support merge/aggregation, for example: <http://www.eclemma.org/jacoco/trunk/doc/merge-mojo.html>.
