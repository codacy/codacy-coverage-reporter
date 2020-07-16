# Commit SHA hash detection

Codacy automatically detects a commit SHA hash from several sources in the following order:

**CI providers**

- Appveyor
- Bitrise
- Buildkite
- Circle CI
- Codefresh
- Codeship
- Docker
- Gitlab
- Greenhouse CI
- Heroku CI
- Jenkins
- Magnum CI
- Semaphore CI
- Shippable CI
- Solano CI
- TeamCity CI
- Travis CI
- Wercker CI

**Git repository directory**

- If it finds a git directory it will get current commit.

**Force commit SHA hash**

- You can force using a specific commit SHA hash with:

```
codacy-coverage-reporter report -l Java --commit-uuid "my_commit_hash" -r coverage.xml
```
