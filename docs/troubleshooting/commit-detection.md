# Commit SHA hash detection

Codacy Coverage Reporter automatically detects a commit SHA hash from several sources in the following order:

1.  **CI/CD platforms**

    -   Appveyor
    -   Bitrise
    -   Buildkite
    -   Circle CI
    -   Codefresh
    -   Codeship
    -   Docker
    -   Gitlab
    -   Greenhouse CI
    -   Heroku CI
    -   Jenkins
    -   Magnum CI
    -   Semaphore CI
    -   Shippable CI
    -   Solano CI
    -   TeamCity CI
    -   Travis CI
    -   Wercker CI

2.  **Git repository directory**

    If Codacy Coverage Reporter finds a Git repository directory it will use the current commit.

3.  **Forcing a specific commit SHA hash**

    You can force using a specific commit SHA hash with the flag `--commit-uuid`.
    
    For example:

    ```bash
    bash <(curl -Ls https://coverage.codacy.com/get.sh) report \
            --language Java \
            --commit-uuid cd4d000083a744cf1617d46af4ec108b79e06bed
    ```
