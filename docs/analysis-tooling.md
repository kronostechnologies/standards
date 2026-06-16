# Analysis tooling

- [Summary](#summary)
- [Processes](#processes)
  - [Alerts](#alerts)
  - [Quality scans](#quality-scans)
  - [Software Composition Analysis (SCA)](#software-composition-analysis-sca)
  - [Security scans](#security-scans)
- [Tools](#tools)
  - [Common](#common)
  - [C#](#c)
  - [Docker](#docker)
  - [Golang](#golang)
  - [Helm](#helm)
  - [Kotlin (Gradle)](#kotlin-gradle)
  - [PHP (Composer)](#php-composer)
  - [Python](#python)
  - [Terraform](#terraform)
  - [Typescript (Yarn)](#typescript-yarn)

## Summary

|                                          |                | **Quality**        |                    |                                    |                        | **Software Composition Analysis** |              |                   | **Security**  |                                                                   |              |
|:-----------------------------------------|:---------------|:-------------------|:-------------------|:-----------------------------------|:-----------------------|:----------------------------------|:-------------|:------------------|:--------------|:------------------------------------------------------------------|:-------------|
|                                          |                | **Code Style**     | **Code Quality**   | **Test Runner**                    | **Test Coverage**      | **SBOM\***                        | **Licenses** | **Vulnerabilities** | **Secrets** | **SAST[^1]**                                                      | **DAST[^2]** |
| **Frequency**                            | *Pull request* | All                | All                | All                                | All                    | All (except Trivy)                | All          | Optional          | All           | Optional                                                          | No           |
|                                          | *Push to main* | Yes                | Yes                | Yes                                | Yes                    | Yes                               | Yes          | Yes               | Yes           | Yes[^1]                                                           | No           |
|                                          | *Commit*       | Head               | Head               | Head                               | Head                   | Head                              | Head         | Head              | All           | Head                                                              | Head         |
|                                          | *Tags*         | v\*                | v\*                | v\*                                | v\*                    | v\*                               | v\*          | v\*               | v\*           | v\*                                                               | v\*          |
|                                          | *Schedule*     | No                 | No                 | No                                 | No                     | No                                | No           | Hourly            | No            | Daily[^1]                                                         | Daily        |
| [**C#**](#c)[^3]                         |                |                    |                    |                                    |                        | [Github Dependency Graph][gdg]    | [Github Dependency Graph][gdg] | [Github Dependency Graph][gdg] | [GHAS][ghas] | [CodeQL](https://codeql.github.com/)                              |              |
| [**Docker**](#docker)                    |                | [Hadolint](https://github.com/hadolint/hadolint) | [Hadolint](https://github.com/hadolint/hadolint) |                                    | | [Trivy](https://github.com/aquasecurity/trivy) | | | | [Trivy](https://github.com/aquasecurity/trivy)                    |              |
| [**Golang**](#golang)                    |                | [gofumpt](https://github.com/mvdan/gofumpt) | [go vet](https://pkg.go.dev/cmd/vet) |                                    | | [Github Dependency Graph][gdg] | | | | [CodeQL](https://codeql.github.com/)                              |              |
| [**Helm**](#helm)                        |                | [Helm lint](https://helm.sh/docs/helm/helm_lint/) | [Helm lint](https://helm.sh/docs/helm/helm_lint/) |                                    | | | | | | [Checkov](https://www.checkov.io/)                                |              |
| [**Kotlin**](#kotlin-gradle)             |                | [Detekt](https://github.com/detekt/detekt) | [Detekt](https://github.com/detekt/detekt) | [JUnit](https://junit.org/junit5/) | [JaCoCo](https://www.eclemma.org/jacoco/) | [CycloneDX Gradle](https://github.com/CycloneDX/cyclonedx-gradle-plugin) | | | | [CodeQL](https://codeql.github.com/)                              |              |
| [**PHP**](#php-composer)                 |                | [CodeSniffer](https://github.com/squizlabs/PHP_CodeSniffer/) | [Psalm](https://psalm.dev/) | [PHPUnit](https://phpunit.de/)     | [PHPUnit](https://phpunit.readthedocs.io/en/9.5/code-coverage-analysis.html) | [Github Dependency Graph][gdg] | | | | [Psalm Taint Analysis](https://psalm.dev/docs/security_analysis/) |              |
| [**Python**](#python)                    |                | [pycodestyle](https://pycodestyle.pycqa.org/) | [pytype](https://google.github.io/pytype/) | [pytest](https://docs.pytest.org/) | | [Github Dependency Graph][gdg] | | | | [CodeQL](https://codeql.github.com/)                              |              |
| [**Terraform**](#terraform)              |                | [tflint](https://github.com/terraform-linters/tflint) | [tflint](https://github.com/terraform-linters/tflint) |                                    | | | | | | [Checkov](https://www.checkov.io/)                                |              |
| [**Javascript / Typescript / CSS**](#typescript-yarn) |                | [ESLint](https://eslint.org/) <br> [StyleLint](https://stylelint.io/) | [ESLint](https://eslint.org/) <br> [StyleLint](https://stylelint.io/) | [Jest](https://jestjs.io/)         | [Jest](https://jestjs.io/) | [Github Dependency Graph][gdg] | | | | [CodeQL](https://codeql.github.com/)                              |              |

[^1]: If a SAST scan is unreasonably long for a specific tool or codebase, we will scope down from continuous scans to daily scans.
[^2]: Dynamic application security testing (DAST) has yet to be tackled.
[^3]: We don't maintain any C# code but recognize it as a supported language within Equisoft.

[gdg]: https://docs.github.com/en/code-security/supply-chain-security/understanding-your-software-supply-chain/about-the-dependency-graph
[ghas]: https://docs.github.com/en/code-security/secret-scanning/about-secret-scanning

> [!IMPORTANT]
> ### Software Bill of Material
>
> [Github Dependency Graph][gdg] (GDG) scans automatically most dependency trees. If an environment cannot be scanned automatically by Github Dependency Graph, extra tooling is at our disposal to generate graphs in CycloneDX format. These files can be uploaded to github with [sbom-dependency-submission-action](https://github.com/evryfs/sbom-dependency-submission-action).
>
> At this time, only **Docker** and **Gradle** are not automatically scanned by Github Dependency Graph.

## Processes

### Alerts

An *alert* as referenced below translates to:

1. A dashboard URL pointing to either [Github Advanced Security](https://docs.github.com/en/code-security/getting-started/github-security-features) or [Github Dependency Graph][gdg].
2. A Slack message in the [#compliance-feed](https://equisoft.slack.com/archives/C03TZQ0KGL8) channel.
3. A Jira ticket with the "github-url" field containing the dashboard URL.

### Quality scans

1. Must be performed for every pull request and default branch.
2. All outstanding warnings and errors must be fixed before merging.

### Software Composition Analysis (SCA)

1. A Software Bill of Material (SBOM) must be calculated for every pull request and default branch.
2. All SBOMs must be published to [Github Dependency Graph][gdg] (GDG).
3. A pull request should block the merge option if it introduces dependencies that have:
   1. Known vulnerabilities
   2. An invalid or non-compliant license
4. Dependency graphs for each default branch must be analyzed daily. An *alert* must be triggered when a dependency has:
   1. Known vulnerabilities
   2. An invalid or non-compliant license

### Security scans

#### Secret Scans

1. All commits should automatically be scanned for potential secret leaks.
2. Each potential secret leak must trigger an *alert*.

#### Static Application Security Testing (SAST)

1. Each default branch should be scanned daily with our preferred SAST scanning tools.
2. Each SARIF report must be uploaded to Github Advanced Security.
3. Every issue with a severity of **high** or **critical** must trigger an *alert*.

## Tools

### Common

| **Tool** | **Scan type** | **Outcome** | **Implementation** |
|:---------|:--------------|:------------|:-------------------|
| Github Actions | | - Consumes events from [Github Advanced Security](https://docs.github.com/en/code-security/getting-started/github-security-features) and [Github Dependency Graph][gdg]. <br> - Creates an *alert* for each vulnerable dependency. <br> - Creates an *alert* for each high or critical defect. <br> - Creates an *alert* for each unauthorized or invalid license. | - [Compliance repository](https://github.com/equisoft/compliance) <br> - [Github webhook to actions bridge](https://github.com/equisoft/github-webhook-to-action-bridge) |
| [Github Advanced Security](https://docs.github.com/en/code-security/getting-started/github-security-features) (GHAS) | - Security operation center <br> - Secret scan | - All security scans (SAST) produced by default branches are published here. <br> - Shipping code is blocked until all potential secret leakages are resolved. <br><br> Github Advanced Security offers additional built-in protections: <br> 1. Commits cannot be pushed at all if a secret is detected. This essentially removes the leak whatsoever. Maintainers can bypass this protection. <br> 2. Detected secrets in *public repositories* can be automatically revoked. See [secret-scanning-partner-program](https://docs.github.com/en/developers/overview/secret-scanning-partner-program). | [Dashboard homepage](https://github.com/enterprises/equisoft/security) |
| [Github Dependency Graph][gdg] (GDG) | Dependency scan | - All SBOM produced by default branches are published here. <br> - Alerts and creates a critical defect when a vulnerable dependency is detected. | - [Dashboard homepage](https://github.com/orgs/kronostechnologies/insights/dependencies) <br> - [SBOM submission action](https://github.com/evryfs/sbom-dependency-submission-action) |

### C\#

Reference project: —

| **Tool** | **Scan type** | **ADR** | **Formats** | **Outcome** | **Implementation** |
|:---------|:--------------|:--------|:------------|:------------|:-------------------|
| [CodeQL](https://codeql.github.com/) | Static Application Security Testing | | SARIF | Shipping code is blocked until all errors are fixed. | [Action](https://github.com/equisoft-actions/codeql) |

### Docker

Reference project: <https://github.com/kronostechnologies/webapp-boilerplate/>

| **Tool** | **Scan type** | **ADR** | **Formats** | **Outcome** | **Implementation** |
|:---------|:--------------|:--------|:------------|:------------|:-------------------|
| [Hadolint](https://github.com/hadolint/hadolint) | - Code Style <br> - Code Quality | | [SARIF](https://github.com/hadolint/hadolint#cli) | Shipping code is blocked until all errors are resolved. | - [Action](https://github.com/equisoft-actions/hadolint) <br> - [Action usage in Micronaut Workflow](https://github.com/equisoft/workflows/blob/main/.github/workflows/micronaut-gradle.yml) <br> - [Action usage in Frontend Workflow](https://github.com/equisoft/workflows/blob/main/.github/workflows/webapp-frontend.yml) |
| [Trivy](https://github.com/aquasecurity/trivy) | - Code Style <br> - Code Quality <br> - Static Application Security Testing | | [SARIF](https://github.com/goodwithtech/dockle#get-or-save-the-results-as-sarif) | Report is uploaded to GHAS for further analysis and tracking. | - [Action](https://github.com/equisoft-actions/docker-sast) <br> - [Action usage in Docker Security Workflow](https://github.com/equisoft-actions/docker-workflows/blob/main/.github/workflows/security.yml) <br> - [Backend Workflow usage](https://github.com/equisoft-actions/kotlin-workflows/blob/main/.github/workflows/micronaut-gradle.yml#LL525C10-L525C10) <br> - [Frontend Workflow usage](https://github.com/equisoft-actions/js-workflows/blob/main/.github/workflows/webapp-frontend.yml#L382) <br> - [PHP Workflow usage](https://github.com/equisoft-actions/js-workflows/blob/main/.github/workflows/webapp-frontend.yml#L382) <br> - [Global script](https://github.com/kronostechnologies/standards/blob/master/bin/docker-sast.sh) |
| [Trivy](https://github.com/aquasecurity/trivy) | Software Bill of Material | | [GitHub](https://docs.github.com/en/rest/dependency-graph/dependency-submission) | SBOM is uploaded to GDG for further analysis and tracking. | - [Action](https://github.com/equisoft-actions/docker-sbom) <br> - [Action usage in Docker Security Workflow](https://github.com/equisoft-actions/docker-workflows/blob/main/.github/workflows/security.yml) <br> - [Backend Workflow usage](https://github.com/equisoft-actions/kotlin-workflows/blob/main/.github/workflows/micronaut-gradle.yml#LL525C10-L525C10) <br> - [Frontend Workflow usage](https://github.com/equisoft-actions/js-workflows/blob/main/.github/workflows/webapp-frontend.yml#L382) <br> - [PHP Workflow usage](https://github.com/equisoft-actions/js-workflows/blob/main/.github/workflows/webapp-frontend.yml#L382) <br> - [Global script](https://github.com/kronostechnologies/standards/blob/master/bin/docker-sbom.sh) |

### Golang

Reference project: <https://github.com/equisoft/backup-operator>

| **Tool** | **Scan type** | **ADR** | **Formats** | **Outcome**                                                | **Implementation** |
|:---------|:--------------|:--------|:------------|:-----------------------------------------------------------|:-------------------|
| [CodeQL](https://codeql.github.com/) | Static Application Security Testing | | SARIF | JIRA tickets are created for each high or critical defect. | [Action](https://github.com/equisoft-actions/codeql) |
| [CycloneDX Gomod](https://github.com/CycloneDX/cyclonedx-gomod/) | Software Bill of Material | | [CycloneDX](https://cyclonedx.org/specification/overview/) | SBOM is uploaded to GDG for further analysis and tracking. | - [Action](https://github.com/equisoft-actions/gomod-sbom) <br> - [Usage in workflow](https://github.com/equisoft/backup-operator/blob/main/.github/workflows/security.yml#L9) |
| [gofumpt](https://github.com/mvdan/gofumpt) | - Code Style <br> - Code Quality | | | Shipping code is blocked until all errors are resolved.    | |
| [go vet](https://pkg.go.dev/cmd/vet) | Code Quality | | | Shipping code is blocked until all errors are resolved.    | |

### Helm

Reference project: —

| **Tool** | **Scan type** | **ADR** | **Formats** | **Outcome** | **Implementation** |
|:---------|:--------------|:--------|:------------|:------------|:-------------------|
| [Helm lint](https://helm.sh/docs/helm/helm_lint/) | - Code Style <br> - Code Quality | [ASR-07: Code Style](../asr/ASR-07_code-style.md) | | Shipping code is blocked until all errors are resolved. | |
| [Checkov](https://www.checkov.io/) | Static Application Security Testing | | SARIF | JIRA tickets are created for each high or critical defect. | |

### Kotlin (Gradle)

Reference project: <https://github.com/kronostechnologies/webapp-boilerplate/tree/master/backend>

| **Tool** | **Scan type** | **ADR** | **Formats** | **Outcome** | **Implementation** |
|:---------|:--------------|:--------|:------------|:------------|:-------------------|
| [CycloneDX Gradle](https://github.com/CycloneDX/cyclonedx-gradle-plugin) | Software Bill of Material | | [CycloneDX](https://cyclonedx.org/specification/overview/) | SBOM is uploaded to GDG for further analysis and tracking. | - [Action](https://github.com/equisoft-actions/gradle-sbom) <br> - [Usage in workflow](https://github.com/kronostechnologies/webapp-boilerplate/blob/master/.github/workflows/security.yml#L20) |
| [Detekt](https://github.com/detekt/detekt) | - Code Style <br> - Code Quality | [ASR-07: Code Style](../asr/ASR-07_code-style.md) | - [XML/Checkstyle](https://detekt.github.io/detekt/reporting.html#xml) <br> - [SARIF](https://detekt.github.io/detekt/reporting.html#sarif) | Shipping code is blocked until all errors are resolved. | - [Plugin implementation](https://github.com/kronostechnologies/standards/tree/master/gradle/kotlin) <br> - [Plugin usage](https://github.com/kronostechnologies/webapp-boilerplate/blob/master/backend/bff/build.gradle.kts#L9) <br> - [Usage in workflow](https://github.com/equisoft/workflows/blob/main/.github/workflows/micronaut-gradle.yml#L264) <br> - [Reusable workflow usage](https://github.com/kronostechnologies/webapp-boilerplate/blob/master/.github/workflows/bff.yml#L13) |
| [JUnit](https://junit.org/junit5/) | Test Runner | [ASR-09: Unit tests](../asr/ASR-09_unit-tests.md) | [XML/JUnit](https://docs.gradle.org/current/userguide/java_testing.html#test_reporting) | Shipping code is blocked until all failing tests are fixed. | - [Plugin usage](https://github.com/kronostechnologies/webapp-boilerplate/blob/master/backend/bff/build.gradle.kts#L182-L190) <br> - [Action](https://github.com/kronostechnologies/actions/tree/main/gradle-junit) <br> - [Usage in workflow](https://github.com/equisoft/workflows/blob/main/.github/workflows/micronaut-gradle.yml#L318-L340) <br> - [Reusable workflow usage](https://github.com/kronostechnologies/webapp-boilerplate/blob/master/.github/workflows/bff.yml#L13) |
| [JaCoCo](https://www.eclemma.org/jacoco/) | Test Coverage | [ASR-09: Unit tests](../asr/ASR-09_unit-tests.md) | [XML/JaCoCo](https://docs.gradle.org/current/userguide/jacoco_plugin.html#sec:jacoco_report_configuration) | | - [Plugin usage](https://github.com/kronostechnologies/webapp-boilerplate/blob/master/backend/bff/build.gradle.kts#L192-L195) <br> - [Action](https://github.com/kronostechnologies/actions/tree/main/gradle-jacoco-check) <br> - [Usage in workflow](https://github.com/equisoft/workflows/blob/main/.github/workflows/micronaut-gradle.yml#L366-L391) <br> - [Reusable workflow usage](https://github.com/kronostechnologies/webapp-boilerplate/blob/master/.github/workflows/bff.yml#L13) |
| [CodeQL](https://codeql.github.com/) | Static Application Security Testing | | SARIF | JIRA tickets are created for each high or critical defect. | |

### PHP (Composer)

Reference project: <https://github.com/kronostechnologies/kronos-fna>

| **Tool** | **Scan type** | **ADR** | **Formats** | **Outcome** | **Implementation** |
|:---------|:--------------|:--------|:------------|:------------|:-------------------|
| [CycloneDX Composer](https://github.com/CycloneDX/cyclonedx-php-composer) | Software Bill of Material | | [CycloneDX](https://cyclonedx.org/specification/overview/) | SBOM is uploaded to GDG for further analysis and tracking. | - [Action](https://github.com/equisoft-actions/composer-sbom) <br> - [Usage in workflow](https://github.com/equisoft-actions/php-workflows/blob/main/.github/workflows/php-security.yml) <br> - [Reusable workflow usage](https://github.com/kronostechnologies/kronos-crm/blob/master/.github/workflows/psalm-taint-analysis.yml) |
| [Psalm](https://psalm.dev/) | - Code Style <br> - Code Quality | [ASR-08: Code Quality Analysis](../asr/ASR-08_code-quality-analysis.md) | - [XML/Checkstyle](https://github.com/vimeo/psalm/issues/1616) <br> - [SARIF](https://psalm.dev/docs/security_analysis/#other-sarif-compatible-software) | Shipping code is blocked until all errors are resolved. | - [Action](https://github.com/equisoft-actions/psalm) <br> - [Action usage](https://github.com/kronostechnologies/kronos-crm/blob/master/.github/workflows/php.yml) |
| [Psalm taint analysis](https://psalm.dev/docs/security_analysis/) | Static Application Security Testing | | [SARIF](https://psalm.dev/docs/security_analysis/#other-sarif-compatible-software) | JIRA tickets are created for each high or critical defect. | - [Action](https://github.com/equisoft-actions/psalm-taint-analysis) <br> - [Usage in workflow](https://github.com/equisoft-actions/php-workflows/blob/main/.github/workflows/php-security.yml) <br> - [Reusable workflow usage](https://github.com/kronostechnologies/kronos-crm/blob/master/.github/workflows/psalm-taint-analysis.yml) |
| [PHP CodeSniffer](https://github.com/squizlabs/PHP_CodeSniffer/) | Code Style | [ASR-07: Code Style](../asr/ASR-07_code-style.md) | [XML/Checkstyle](https://github.com/squizlabs/PHP_CodeSniffer/wiki/Reporting#printing-a-checkstyle-report) | Shipping code is blocked until all errors are resolved. | |
| [PHPUnit](https://phpunit.de/) | Test Runner | [ASR-09: Unit tests](../asr/ASR-09_unit-tests.md) | [XML/JUnit](https://phpunit.readthedocs.io/en/9.5/configuration.html?highlight=junit#the-junit-element) | Shipping code is blocked until all failing tests are fixed. | - [Action](https://github.com/equisoft-actions/phpunit) <br> - [Usage in workflow](https://github.com/kronostechnologies/kronos-crm/blob/master/.github/workflows/php.yml) |
| [PHPUnit](https://phpunit.de/) | Test coverage | [ASR-09: Unit tests](../asr/ASR-09_unit-tests.md) | Clover | | |

### Python

Reference project: -

| **Tool** | **Scan type** | **ADR** | **Formats** | **Outcome** | **Implementation** |
|:---------|:--------------|:--------|:------------|:------------|:-------------------|
| [CycloneDX Python](https://github.com/CycloneDX/cyclonedx-python) | Software Bill of Material | | [CycloneDX](https://cyclonedx.org/specification/overview/) | SBOM is uploaded to GDG for further analysis and tracking. | - [Action](https://github.com/CycloneDX/cyclonedx-python) <br> - [Usage in workflow](https://github.com/equisoft-actions/python-workflows/blob/main/.github/workflows/python-security.yml#L80-L99) <br> - [Reusable workflow usage](https://github.com/kronostechnologies/lead_scoring/blob/master/.github/workflows/security-reports.yml#L9-L20) |
| [CodeQL](https://codeql.github.com/) | Static Application Security Testing | | SARIF | JIRA tickets are created for each high or critical defect. | - [Action](https://github.com/equisoft-actions/codeql) <br> - [Usage in workflow](https://github.com/equisoft-actions/python-workflows/blob/main/.github/workflows/python-security.yml#L42-L46) <br> - [Reusable workflow usage](https://github.com/kronostechnologies/lead_scoring/blob/master/.github/workflows/security-reports.yml#L9-L20) |
| [pycodestyle](https://pycodestyle.pycqa.org/) | Code Style | [ASR-07: Code Style](../asr/ASR-07_code-style.md) | | Shipping code is blocked until all errors are fixed. | - [Usage in workflow](https://github.com/equisoft-actions/python-workflows/blob/main/.github/workflows/python-pipenv.yml#L83-L96) <br> - [Reusable workflow usage](https://github.com/kronostechnologies/lead_scoring/blob/master/.github/workflows/quality-checks.yml#L9-L13) |
| [pytype](https://google.github.io/pytype/) | Code Quality | [ASR-07: Code Style](../asr/ASR-07_code-style.md) | | Shipping code is blocked until all errors are fixed. | - [Usage in workflow](https://github.com/equisoft-actions/python-workflows/blob/main/.github/workflows/python-pipenv.yml#L68-L81) <br> - [Reusable workflow usage](https://github.com/kronostechnologies/lead_scoring/blob/master/.github/workflows/quality-checks.yml#L9-L13) |
| [pytest](https://docs.pytest.org/) | Test Runner | [ASR-09: Unit tests](../asr/ASR-09_unit-tests.md) | | Shipping code is blocked until all errors are fixed. | - [Usage in workflow](https://github.com/equisoft-actions/python-workflows/blob/main/.github/workflows/python-pipenv.yml#L53-L66) <br> - [Reusable workflow usage](https://github.com/kronostechnologies/lead_scoring/blob/master/.github/workflows/quality-checks.yml#L9-L13) |

### Terraform

Reference project: —

| **Tool** | **Scan type** | **ADR** | **Formats** | **Outcome** | **Implementation** |
|:---------|:--------------|:--------|:------------|:------------|:-------------------|
| [tflint](https://github.com/terraform-linters/tflint) | - Code Style <br> - Code Quality | [ASR-07: Code Style](../asr/ASR-07_code-style.md) | | Shipping code is blocked until all errors are resolved. | |
| [Checkov](https://www.checkov.io/) | Static Application Security Testing | | SARIF | JIRA tickets are created for each high or critical defect. | |

### Typescript (Yarn)

Reference project: <https://github.com/kronostechnologies/webapp-boilerplate/tree/master/web>

| **Tool** | **Scan type** | **ADR** | **Formats** | **Outcome** | **Implementation** |
|:---------|:--------------|:--------|:------------|:------------|:-------------------|
| [ESLint](https://eslint.org/) | - Code Style (Javascript and Typescript) <br> - Code Quality (Javascript and Typescript) <br> - Many possible plugins to support the wide ecosystem. | [ASR-07: Code Style](../asr/ASR-07_code-style.md) | - [XML/Checkstyle](https://eslint.org/docs/user-guide/formatters/#checkstyle) <br> - [SARIF](https://github.com/microsoft/sarif-js-sdk/tree/main/packages/eslint-formatter-sarif) | Shipping code is blocked until all errors are resolved. | - [Action](https://github.com/kronostechnologies/actions/tree/main/yarn-eslint) <br> - [Usage in workflow](https://github.com/equisoft/workflows/blob/main/.github/workflows/webapp-frontend.yml#L184-L212) <br> - [Reusable workflow usage](https://github.com/kronostechnologies/webapp-boilerplate/blob/master/.github/workflows/web.yml#L13-L28) |
| [Stylelint](https://stylelint.io/) | Code Style (SCSS) | [ASR-07: Code Style](../asr/ASR-07_code-style.md) | [SARIF](https://github.com/zhanwang626/stylelint-sarif-formatter) | Shipping code is blocked until all errors are resolved. | - [Action](https://github.com/kronostechnologies/actions/tree/main/yarn-stylelint) <br> - [Usage in workflow](https://github.com/equisoft/workflows/blob/main/.github/workflows/webapp-frontend.yml#L214-L242) <br> - [Reusable workflow usage](https://github.com/kronostechnologies/webapp-boilerplate/blob/master/.github/workflows/web.yml#L13-L28) |
| [Jest](https://jestjs.io/) | Test Runner (Javascript and Typescript) | [ASR-09: Unit tests](../asr/ASR-09_unit-tests.md) | [XML/Junit](https://github.com/jest-community/jest-junit) | Shipping code is blocked until all failing tests are fixed. | - [Action](https://github.com/kronostechnologies/actions/tree/main/yarn-jest) <br> - [Usage in workflow](https://github.com/equisoft/workflows/blob/main/.github/workflows/webapp-frontend.yml#L244-L272) <br> - [Reusable workflow usage](https://github.com/kronostechnologies/webapp-boilerplate/blob/master/.github/workflows/web.yml#L13-L28) |
| [CodeQL](https://codeql.github.com/) | Static Application Security Testing | | SARIF | JIRA tickets are created for each high or critical defect. | - [Action](https://github.com/equisoft-actions/codeql) <br> - [Usage in workflow](https://github.com/kronostechnologies/webapp-boilerplate/blob/main/.github/workflows/security.yml#L42-L52) |
