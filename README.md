# codacy-coverage-reporter
[![Codacy Badge](https://api.codacy.com/project/badge/grade/1c524e61cd8640e79b80d406eda8754b)](https://www.codacy.com/app/Codacy/codacy-coverage-reporter)
[![Build Status](https://circleci.com/gh/codacy/codacy-coverage-reporter.png?style=shield&circle-token=:circle-token)](https://circleci.com/gh/codacy/codacy-coverage-reporter)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.codacy/codacy-coverage-reporter/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.codacy/codacy-coverage-reporter)

Multi-language coverage reporter for Codacy https://www.codacy.com

```
codacy-coverage-reporter will only work with:
  * Java JRE 7 and higher
```

## Setup

Codacy assumes that coverage is previously configured for your project.
The official coverage tools we support for Java are JaCoCo and Cobertura.

You can install the coverage reporter by running:

### [Install jpm](https://www.jpm4j.org/#!/md/install)
```
curl https://www.jpm4j.org/install/script | sh
```

### Install codacy-coverage-reporter
```
jpm install com.codacy:codacy-coverage-reporter:assembly
```

## Updating Codacy

To update Codacy, you will need your project API token. You can find the token in Project -> Settings -> Integrations -> Project API.

Then set it in your terminal, replacing %Project_Token% with your own token:

```
export CODACY_PROJECT_TOKEN=%Project_Token%
```

**Enterprise**

To send coverage in the enterprise version you should:
```
export CODACY_API_BASE_URL=<Codacy_instance_URL>:16006
```

**Upload coverage**

Next, simply run the Codacy reporter. It will find the current commit and send all details to your project dashboard:

```
codacy-coverage-reporter -l Java -r coverage.xml
```

> Note: You should keep your API token well **protected**, as it grants owner permissions to your projects.

## Java 6

Due to a limitation in Java 6, the plugin is unable to establish a connection to codacy.com.
You can run [this script](https://gist.github.com/mrfyda/51cdf48fa0722593db6a) after the execution to upload the generated report to Codacy.


## Build from source

If you are having any issues with your installation, you can also build the coverage reporter from source.

To make sure you are using the version that you are building, you can remove your previously installed version:
```
[sudo] jpm remove codacy-coverage-reporter
```

1- Clone our repository https://github.com/codacy/codacy-coverage-reporter

2- Run the command `sbt assembly`. This will produce a .jar that you can run in the `codacy-coverage-reporter/target/codacy-coverage-reporter-assembly-1.0.4.jar`

3- In the project you want to send the coverage, use the jar. Example:

```
~/git/codacy/java-project$ java -cp ../codacy-coverage-reporter/target/codacy-coverage-reporter-assembly-1.0.4.jar com.codacy.CodacyCoverageReporter -l Java -r jacoco.xml
```

## Gradle task

A big shout-out to [tompahoward](https://github.com/tompahoward), you can create a gradle task as suggested in https://github.com/mountain-pass/hyperstate/commit/857ca93e1c8484c14a5e2da9f0434d3daf3328ce

```gradle
task uploadCoverageToCodacy(type: JavaExec, dependsOn : jacocoTestReport) {
   main = "com.codacy.CodacyCoverageReporter"
   classpath = configurations.codacy
   args = [
            "-l",
            "Java",
            "-r",
            "${buildDir}/test-results/jacoco/${archivesBaseName}.xml"
           ]
}

task (codacyDepsize) << {
def size = 0;
configurations.codacy.collect { it.length() / (1024 * 1024) }.each { size += it }
println "Total dependencies size: ${Math.round(size * 100) / 100} Mb"

configurations
        .codacy
        .sort { -it.length() }
        .each { println "${it.name} : ${Math.round(it.length() / (1024) * 100) / 100} kb" }
}

task (codacyLocs) << {
    configurations.codacy.each {
        String jarName = it
        println jarName
    }
}
```

## Community supported alternatives

### Maven plugin

Thanks to the amazing job of [halkeye](https://github.com/halkeye) you can now send your coverage to Codacy using a [maven plugin](https://github.com/halkeye/codacy-maven-plugin)!

Just follow the [instructions on his repository](https://github.com/halkeye/codacy-maven-plugin/blob/master/README.md#usage).

## Troubleshooting

If you receive a `Failed to upload report: Not Found`error when running the command, then you'll probably have codacy-coverage-reporter 1.0.3 installed. Make sure you install version 1.0.4, that fixes that error.

Example (issue: [#11](https://github.com/codacy/codacy-coverage-reporter/issues/11)) : 
```
codacy-coverage-reporter -l Java -r PATH_TO_COVERAGE/coverage.xml
2015-11-20 04:06:58,887 [info]  com.codacy Parsing coverage data... 
2015-11-20 04:06:59,506 [info]  com.codacy Uploading coverage data... 

2015-11-20 04:07:00,639 [error] com.codacy Failed to upload report: Not Found
```
Even after doing all of the above troubleshooting steps in case you still encounter the same error 

```
2015-11-20 04:07:00,639 [error] com.codacy Failed to upload report: Not Found 
```

Please try running the command with a --prefix option with path to your code  as shown below , it helps to locate the files for which code coverage is desired

```
codacy-coverage-reporter -l Java -r PATH_TO_COVERAGE/coverage.xml --prefix PATH_TO_THE_DIRECTORY 
```

Example

```
codacy-coverage-reporter -l Java -r api/target/site/jacoco/jacoco.xml --prefix api/src/main/java/
```

## What is Codacy?

[Codacy](https://www.codacy.com/) is an Automated Code Review Tool that monitors your technical debt, helps you improve your code quality, teaches best practices to your developers, and helps you save time in Code Reviews.

### Among Codacy’s features:

 - Identify new Static Analysis issues
 - Commit and Pull Request Analysis with GitHub, BitBucket/Stash, GitLab (and also direct git repositories)
 - Auto-comments on Commits and Pull Requests
 - Integrations with Slack, HipChat, Jira, YouTrack
 - Track issues Code Style, Security, Error Proneness, Performance, Unused Code and other categories

Codacy also helps keep track of Code Coverage, Code Duplication, and Code Complexity.

Codacy supports PHP, Python, Ruby, Java, JavaScript, and Scala, among others.

### Free for Open Source

Codacy is free for Open Source projects.
