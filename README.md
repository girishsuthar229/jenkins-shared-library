# Jenkins Shared Library — Playwright CI/CD Pipeline

A reusable **Jenkins Shared Library** that provides ready-made pipeline steps for running [Playwright](https://playwright.dev/) end-to-end tests and publishing **HTML** + **Allure** reports.

Import this library into **any** project, create a `Jenkinsfile`, and get a fully working CI/CD pipeline — checkout → install → test → report — with zero boilerplate.

---

## 📁 Folder Structure

```
jenkins-shared-library/
├── vars/                                # Global pipeline steps (callable from any Jenkinsfile)
│   ├── checkoutCode.groovy              # Clone the project repo (main branch)
│   ├── installDependencies.groovy       # npm ci / install + Playwright browsers
│   ├── runPlaywrightTests.groovy        # Execute Playwright tests (pass/fail safe)
│   ├── publishReport.groovy             # Publish Playwright HTML report
│   ├── publishAllureReport.groovy       # Publish Allure report from allure-results/
│   └── publishToGitHubPages.groovy      # Push Allure report to GitHub Pages + email
│
├── src/
│   └── com/company/
│       └── PipelineUtils.groovy         # (Reserved) Utility classes for advanced pipeline logic
│
├── resources/
│   └── templates/                       # (Reserved) Templates (email bodies, configs, etc.)
│
└── README.md
```

---

## 🔧 Shared Steps Reference

### `checkoutCode(repoUrl)`

Clones the given Git repository (branch: `main`).

| Parameter | Type   | Required | Description                    |
|-----------|--------|----------|--------------------------------|
| `repoUrl` | String | ✅       | HTTPS / SSH URL of the repo    |

```groovy
checkoutCode('https://github.com/your-org/your-project.git')
```

---

### `installDependencies()`

Installs Node.js dependencies and Playwright browsers. Automatically detects Unix / Windows.

| OS      | Commands                                              |
|---------|-------------------------------------------------------|
| Windows | `npm ci \|\| npm install` → `npx playwright install`  |

```groovy
installDependencies()
```

---

### `runPlaywrightTests()`

Runs `npx playwright test`. If tests **fail**, the build is marked **UNSTABLE** (not FAILURE) so downstream report stages still execute.

```groovy
runPlaywrightTests()
```

---

### `publishReport()`

Publishes the **Playwright HTML Report** via the [HTML Publisher](https://plugins.jenkins.io/htmlpublisher/) plugin.

| Setting                | Value                |
|------------------------|----------------------|
| Report directory       | `playwright-report`  |
| Report entry file      | `index.html`         |
| Report display name    | *Playwright Report*  |
| Keep all past reports  | `true`               |

```groovy
publishReport()
```

---

### `publishAllureReport()`

Publishes the **Allure Report** from the `allure-results/` directory via the [Allure Jenkins Plugin](https://plugins.jenkins.io/allure-jenkins-plugin/).

```groovy
publishAllureReport()
```

> **Note:** Your Playwright config must include the Allure reporter so that test results are written to `allure-results/`.

---

### `publishToGitHubPages(config)`

Pushes the generated Allure report to a **GitHub Pages** branch and optionally sends an **email notification**.

| Parameter       | Type   | Required | Default                  | Description                                   |
|-----------------|--------|----------|--------------------------|-----------------------------------------------|
| `repoUrl`       | String | ✅       | —                        | GitHub repo URL                               |
| `credentialsId` | String | ✅       | —                        | Jenkins credentials ID for the GitHub token   |
| `branch`        | String | ❌       | `gh-pages`               | Branch to push the report to                  |
| `reportDir`     | String | ❌       | `allure-report`          | Local directory containing the generated report |
| `gitUser`       | String | ❌       | `Jenkins CI`             | Git committer name                            |
| `gitEmail`      | String | ❌       | `jenkins@example.com`    | Git committer email                           |
| `publicUrl`     | String | ❌       | `''`                     | Public URL to print in the console log        |
| `emailTo`       | String | ❌       | `''`                     | Email recipient(s) for the results notification |

```groovy
publishToGitHubPages(
    repoUrl      : 'https://github.com/your-org/your-project.git',
    credentialsId: 'github-token',
    publicUrl    : 'https://your-org.github.io/your-project/',
    emailTo      : 'team@example.com'
)
```

---

## 🚀 How to Use in Your Project

### 1. Register the Shared Library in Jenkins

1. Go to **Manage Jenkins → System → Global Pipeline Libraries**.
2. Add a new library:

   | Field             | Value                                                       |
   |-------------------|-------------------------------------------------------------|
   | Name              | `jenkins-shared-library`                                    |
   | Default version   | `main`                                                      |
   | Retrieval method  | **Modern SCM → Git**                                        |
   | Project repository| URL of this `jenkins-shared-library` repo                   |

3. Save.

---

### 2. Create a `Jenkinsfile` in Your Project

Add a `Jenkinsfile` at the root of your test project:

```groovy
@Library('jenkins-shared-library') _

pipeline {
    agent any

    tools {
        nodejs 'NodeJS'   // Name must match the NodeJS installation configured in Jenkins
    }

    stages {

        stage('📥 Checkout Code') {
            steps {
                checkoutCode('https://github.com/your-org/your-test-project.git')
            }
        }

        stage('📦 Install Dependencies') {
            steps {
                installDependencies()
            }
        }

        stage('🧪 Run Playwright Tests') {
            steps {
                runPlaywrightTests()
            }
        }

        stage('📊 Publish Playwright HTML Report') {
            steps {
                publishReport()
            }
        }

        stage('📈 Publish Allure Report') {
            steps {
                publishAllureReport()
            }
        }

        // Optional — push reports to GitHub Pages & send email
        stage('🌐 Publish to GitHub Pages') {
            steps {
                publishToGitHubPages(
                    repoUrl      : 'https://github.com/your-org/your-test-project.git',
                    credentialsId: 'github-token',
                    publicUrl    : 'https://your-org.github.io/your-test-project/',
                    emailTo      : 'qa-team@example.com'
                )
            }
        }
    }

    post {
        always {
            echo '🏁 Pipeline finished.'
        }
        success {
            echo '✅ All tests passed!'
        }
        unstable {
            echo '⚠️ Some tests failed — check the reports.'
        }
        failure {
            echo '❌ Pipeline failed.'
        }
    }
}
```

---

### 3. Playwright Config — Enable Allure Reporter

In your project's `playwright.config.ts`, add the Allure reporter so that results are generated for the Allure stage:

```typescript
import { defineConfig } from '@playwright/test';

export default defineConfig({
  reporter: [
    ['html', { open: 'never' }],                       // Playwright HTML report
    ['allure-playwright', { outputFolder: 'allure-results' }]  // Allure results
  ],
  // ... rest of your config
});
```

Install the Allure Playwright reporter in your project:

```bash
npm install -D allure-playwright
```

---

## ✅ Jenkins Prerequisites

Make sure the following are installed / configured on your Jenkins instance:

| Requirement                   | Purpose                                        |
|-------------------------------|------------------------------------------------|
| **NodeJS Plugin**             | Provides the `nodejs` tool in pipelines        |
| **HTML Publisher Plugin**     | `publishReport()` — Playwright HTML report     |
| **Allure Plugin**             | `publishAllureReport()` — Allure report        |
| **Allure Commandline**        | Configured in **Global Tool Configuration**    |
| **Email Extension Plugin**    | `publishToGitHubPages()` — email notifications |
| **Git Plugin**                | `checkoutCode()` — SCM checkout                |
| **GitHub Token (credential)** | `publishToGitHubPages()` — push to gh-pages    |

---

## 📋 Pipeline Flow

```
┌──────────────────┐
│  Checkout Code   │  ← Clone your test project repo
└────────┬─────────┘
         ▼
┌──────────────────┐
│ Install Deps     │  ← npm ci + Playwright browsers
└────────┬─────────┘
         ▼
┌──────────────────┐
│ Run Tests        │  ← npx playwright test (pass / fail safe)
└────────┬─────────┘
         ▼
┌──────────────────────────────────────┐
│ Publish Reports                      │
│  ├─ Playwright HTML  (index.html)    │
│  ├─ Allure Report    (allure-results)│
│  └─ GitHub Pages     (optional)      │
└──────────────────────────────────────┘
```

---

## 📝 Notes

- **Cross-platform**: Every step auto-detects Unix vs Windows and runs the appropriate shell commands (`sh` / `bat`).
- **Fail-safe tests**: `runPlaywrightTests()` catches test failures and marks the build as `UNSTABLE`, so reports are always published even when tests fail.
- **Extensible**: Add new steps by creating a new `.groovy` file in `vars/` — it will be available as a global function in any Jenkinsfile that imports this library.

---

## 📄 License

Internal use — adapt as needed for your organization.
