# Codacy Coverage Reporter

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/1c524e61cd8640e79b80d406eda8754b)](https://www.codacy.com/gh/codacy/codacy-coverage-reporter?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=codacy/codacy-coverage-reporter&amp;utm_campaign=Badge_Grade)
[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/1c524e61cd8640e79b80d406eda8754b)](https://www.codacy.com/gh/codacy/codacy-coverage-reporter?utm_source=github.com&utm_medium=referral&utm_content=codacy/codacy-coverage-reporter&utm_campaign=Badge_Coverage)
[![Build Status](https://circleci.com/gh/codacy/codacy-coverage-reporter.png?style=shield&circle-token=:circle-token)](https://circleci.com/gh/codacy/codacy-coverage-reporter)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.codacy/codacy-coverage-reporter/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.codacy/codacy-coverage-reporter)

Multi-language coverage reporter for Codacy https://www.codacy.com

## Setup

Follow the instructions on how to [add coverage to your repository](https://docs.codacy.com/coverage-reporter/adding-coverage-to-your-repository/).

For a complete list of commands and options, run the Codacy Coverage Reporter with the flag `--help`. For example:

```bash
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
  --codacy-api-base-url  <the base URL for the Codacy API>
  --commit-uuid  <your commitUUID>
  --skip | -s  <skip if token isn't defined>
  --language | -l  <your project language>
  --coverage-reports | -r  <your project coverage file name>
  --partial  <if the report is partial>
  --prefix  <the project path prefix>
  --force-coverage-parser  <your coverage parser>
        Available parsers are: opencover,clover,lcov,phpunit,jacoco,dotcover,cobertura


 --> Succeeded!
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
