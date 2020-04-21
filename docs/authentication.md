# Authentication

## Authenticating using an Account API token

If you'd like to automate uploading coverage for multiple repositories you can authenticate using an Account API Token:

1. Create an Account API token on Codacy. You can find the token in *Your account* → *API tokens*
1. Set the API token in your terminal, replacing *%API_Token%* with your own token
1. Set your repository name in your terminal, replacing *%Repo_Name%*
1. Set your username in your terminal, replacing *%Username%*

```
export CODACY_API_TOKEN=%API_Token%
export CODACY_PROJECT_NAME=%Repo_Name%
export CODACY_USERNAME=%Username%
```

⚠️ **You should keep your API token well protected, as it grants owner permissions to your projects.**
