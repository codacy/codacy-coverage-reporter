# Swift & Objective-C support

To use Swift/Objective-C and XCode coverage reporter, you should use [Slather](https://github.com/SlatherOrg/slather) to convert XCode report into Cobertura format.

This can be achieve by running this commands on the CI:

```bash
gem install slather
slather coverage -x --output-directory <report-output-dir> --scheme <project-name> <project-name>.xcodeproj
```

This will generate a `cobertura.xml` inside `<report-output-dir>` folder.
After this, call our script:

```bash
bash <(curl -Ls https://coverage.codacy.com/get.sh)
```
