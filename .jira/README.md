# 🌍 Jira Environment Mapping for Deployments

_see
also: [Environment Mapping Docs](https://github.com/atlassian/github-for-jira/blob/main/docs/deployments.md#environment-mapping)_

This repo uses `.jira/config.yml` to map deployment environments in Jira tickets, making sure Jira recognizes the
correct environment type.

## 🚀 How it works:

1️⃣ **Jira listens** to GitHub deployment events and checks the `environment name`.  
2️⃣ **Jira reads** the issue keys from commit messages (e.g., `JIRA-123`).  
3️⃣ **Jira maps** the environment name to a type (e.g., `development`, `testing`, `staging`, `production`) based on
`.jira/config.yml` in the `main` branch. and updates the ticket.
