# codacy-coverage-reporter
[![Build Status](https://circleci.com/gh/codacy/codacy-coverage-reporter.png?style=shield&circle-token=:circle-token)](https://circleci.com/gh/codacy/codacy-coverage-reporter)
[![Codacy Badge](https://www.codacy.com/project/badge/1c524e61cd8640e79b80d406eda8754b)](https://www.codacy.com/app/Codacy/codacy-coverage-reporter)

Multi-language coverage reporter for Codacy https://www.codacy.com

```
codacy-coverage-reporter will only work with:
  * Java JRE 7 and higher
```

## Setup

Codacy assumes that coverage is previously configured for your project.

You can install the coverage reporter by running:

### [Install jpm](https://www.jpm4j.org/#!/md/install)
```
curl http://www.jpm4j.org/install/script | sh
```

### Install codacy-coverage-reporter
```
jpm install com.codacy:codacy-coverage-reporter
```

## Updating Codacy

To update Codacy, you will need your project API token. You can find the token in Project -> Settings -> Integrations -> Project API.

Then set it in your terminal, replacing %Project_Token% with your own token:

```
export CODACY_PROJECT_TOKEN=%Project_Token%
```

Next, simply run the Codacy reporter. It will find the current commit and send all details to your project dashboard:

```
codacy-coverage-reporter -l Java -r coverage.xml
```

## Java 6

Due to a limitation in Java 6, the plugin is unable to establish a connection to codacy.com.
You can run [this script](https://gist.github.com/mrfyda/51cdf48fa0722593db6a) after the execution to upload the generated report to Codacy.
