# Generating coverage reports

Code coverage is a metric used to describe the degree to which the source code of a program is tested. A program with high code coverage has been more thoroughly tested and has a lower chance of containing software bugs than a program with low code coverage. You can read more about the [basics of code coverage](https://blog.codacy.com/a-guide-to-code-coverage-part-1-code-coverage-explained/) on our blog.   

There are many tools that you can use to generate coverage reports for the languages used in your repositories. The following table contains example coverage tools that generate reports in formats that Codacy supports:

| Language          | Example coverage tools                                                                                                                                                                        | Report formats                                                                     |
| ----------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------- |
| C#                | [OpenCover](https://github.com/OpenCover/opencover)<br/>[dotCover CLI](https://www.jetbrains.com/help/dotcover/Running_Coverage_Analysis_from_the_Command_LIne.html)                          | OpenCover<br/>dotCover XML                                                         |
| Java              | [JaCoCo](http://eclemma.org/jacoco/)<br/>[Cobertura](http://cobertura.github.io/cobertura/)                                                                                                   | JaCoCo<br/>Cobertura                                                               |
| JavaScript        | [Istanbul](https://github.com/gotwarlost/istanbul)<br/>[Poncho](https://github.com/deepsweet/poncho)<br/>[Mocha](http://mochajs.org/) + [Blanket.js](https://github.com/alex-seville/blanket) | LCOV                                                                               |
| PHP               | [PHPUnit](https://phpunit.readthedocs.io/en/9.3/code-coverage-analysis.html)                                                                                                                  | PHPUnit XML (version &lt;= 4)<br/>Clover                                           |
| Python            | [Coverage.py](https://coverage.readthedocs.io/en/coverage-5.0.3/)                                                                                                                             | Cobertura                                                                          |
| Ruby              | [SimpleCov](https://github.com/colszowka/simplecov)                                                                                                                                           | Cobertura<br/>LCOV                                                                 |
| Scala             | [sbt-jacoco](https://www.scala-sbt.org/sbt-jacoco/)<br/>[scoverage](http://scoverage.org/)                                                                                                    | JaCoCo<br/>Cobertura                                                               |
| Swift/Objective-C | [Xcode](https://developer.apple.com/library/archive/documentation/DeveloperTools/Conceptual/testing_with_xcode/chapters/07-code_coverage.html) Code Coverage                                  | See below how to [generate coverage reports with Xcode](#swift-objectivec-support) |

## Swift and Objective-C support {: id="swift-objectivec-support"}

To use Swift and Objective-C with Xcode coverage reports, use [Slather](https://github.com/SlatherOrg/slather) to convert the Xcode output into the Cobertura format.

To do this, execute the following commands on the CI:

```bash
gem install slather
slather coverage -x --output-directory <report-output-dir> --scheme <project-name> <project-name>.xcodeproj
```

This will generate a file `cobertura.xml` inside the folder `<report-output-dir>`.

After this, run Codacy Coverage Reporter to [upload the coverage results to Codacy](adding-coverage-to-your-repository.md).
