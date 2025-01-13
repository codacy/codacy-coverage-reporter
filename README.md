# Codacy Coverage Reporter

[![Codacy Badge](https://app.codacy.com/project/badge/Grade/c56384a7b0044caea298480b9fde2522)](https://www.codacy.com/gh/codacy/codacy-coverage-reporter/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=codacy/codacy-coverage-reporter&amp;utm_campaign=Badge_Grade)
[![Build Status](https://circleci.com/gh/codacy/codacy-coverage-reporter.png?style=shield&circle-token=:circle-token)](https://circleci.com/gh/codacy/codacy-coverage-reporter)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.codacy/codacy-coverage-reporter/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.codacy/codacy-coverage-reporter)

Multi-language coverage reporter for Codacy https://www.codacy.com

## Setup

Follow the instructions on how to [add coverage to your repository](https://docs.codacy.com/coverage-reporter/adding-coverage-to-your-repository/).

If necessary, see [alternative ways of running Codacy Coverage Reporter](https://docs.codacy.com/coverage-reporter/alternative-ways-of-running-coverage-reporter/) for other ways of running Codacy Coverage Reporter, such as by installing the binary manually or using a CircleCI Orb or the [Codacy Coverage Reporter GitHub Action](https://github.com/codacy/codacy-coverage-reporter-action).

For a complete list of commands and options, run the Codacy Coverage Reporter with the flag `--help`. For example:

```
$ bash <(curl -Ls https://coverage.codacy.com/get.sh) report --help
     ______          __
    / ____/___  ____/ /___ ________  __
   / /   / __ \/ __  / __ `/ ___/ / / /
  / /___/ /_/ / /_/ / /_/ / /__/ /_/ /
  \____/\____/\__,_/\__,_/\___/\__, /
                              /____/

  Codacy Coverage Reporter

 --> Using codacy reporter codacy-coverage-reporter-linux from cache
Command: report
Usage: codacy-coverage-reporter report 
  --project-token | -t  <your project API token>
  --api-token | -a  <your account API token>
  --organization-provider  <the project organization provider> (manual, gh, bb, ghe, bbe, gl, gle)
  --username | -u  <the project owner name>
  --project-name | -p  <your project name>
  --codacy-api-base-url  <the base URL for the Codacy API>
  --commit-uuid  <your commit SHA-1 hash>
  --http-timeout  <Sets a specified read timeout value, in milliseconds, to be used when interacting with Codacy API. By default, the value is 10 seconds>
  --skip | -s  <skip if token isn't defined>
  --sleep-time <Sets a specified time, in milliseconds, to be used when waiting between retries. By default, the value is 10 seconds>
  --num-retries <Sets a number of retries in case of failure. By default, the value is 3 times>
  --language | -l  <language associated with your coverage report>
  --coverage-reports | -r  <your project coverage file name (supports globs)>
  --partial  <if the report is partial>
  --prefix  <the project path prefix>
  --skip-ssl-verification` [default: false] - Skip the SSL certificate verification when communicating with the Codacy API
  --force-coverage-parser  <your coverage parser>
        Available parsers are: opencover,clover,lcov,phpunit,jacoco,dotcover,cobertura,go


 --> Succeeded!
```

## What is Codacy?

[Codacy](https://www.codacy.com/) is an Automated Code Review Tool that monitors your technical debt, helps you improve your code quality, teaches best practices to your developers, and helps you save time in Code Reviews.

### Among Codacy’s features:

- Identify new Static Analysis issues
- Commit and Pull Request Analysis with GitHub, GitLab, and Bitbucket
- Auto-comments on Commits and Pull Requests
- Integrations with Slack and Jira
- Track issues in Code Style, Security, Error Proneness, Performance, Unused Code and other categories

Codacy also helps keep track of Code Coverage, Code Duplication, and Code Complexity.

Codacy supports PHP, Python, Ruby, Java, JavaScript, and Scala, among others.

### Free for Open Source

Codacy is free for Open Source projects.
