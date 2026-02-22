# 🏦 WellsFargo Automation Framework

Selenium + Cucumber + TestNG automation framework for testing [TutorialsNinja](https://tutorialsninja.com/demo/) e-commerce application.

---

## 🧰 Tech Stack

| Category | Tool | Version |
|----------|------|---------|
| Language | Java | 21 LTS |
| Build | Maven | 3.9.11 |
| Test Framework | TestNG | 7.10.2 |
| BDD | Cucumber | 7.15.0 |
| Automation | Selenium WebDriver | 4.19.1 |
| Reporting | ExtentReports | 5.1.2 |
| Reporting | QMetry (Custom) | Summary, JSON, HyperExecute |
| Cloud Testing | LambdaTest HyperExecute | CLI |
| CI/CD | GitHub Actions | 3 workflows |
| CI/CD | Jenkins | 2.541.2 |
| Version Control | Git + GitHub | sathisho/WellsFargo |
| Email | Jakarta Mail | 2.0.3 |
| Logging | Log4j2 | - |

---

## 📁 Project Structure

```
WellsFargo/
├── pom.xml                          # Maven config & dependencies
├── Jenkinsfile                      # Jenkins pipeline
├── hyperexecute.yaml                # HyperExecute cloud config
├── .github/workflows/
│   ├── ci-smoke.yml                 # CI - Smoke tests on push/PR
│   ├── cd-regression.yml            # CD - Regression on main
│   └── hyperexecute.yml             # HyperExecute nightly/manual
├── src/
│   ├── main/java/
│   │   ├── config/
│   │   │   ├── ConfigReader.java    # Properties file reader
│   │   │   └── Log4j2.xml           # Logging config
│   │   ├── pages/
│   │   │   ├── CheckoutPage.java    # Checkout Page Object
│   │   │   ├── LoginPage.java       # Login Page Object
│   │   │   ├── HomePage.java        # Home Page Object
│   │   │   └── SearchPage.java      # Search Page Object
│   │   └── utils/
│   │       ├── DriverFactory.java   # WebDriver (local + remote)
│   │       ├── ExtentManager.java   # Extent report setup
│   │       ├── QMetryReportManager.java  # QMetry reports + email
│   │       ├── Log.java             # Logger utility
│   │       └── TestUtil.java        # Test helpers
│   └── test/
│       ├── java/
│       │   ├── runners/
│       │   │   └── TestRunner.java  # Cucumber TestNG runner
│       │   └── stepdefinitions/
│       │       ├── Hooks.java       # Before/After hooks
│       │       ├── CheckoutSteps.java   # Checkout steps
│       │       └── LoginSteps.java      # Login steps
│       └── resources/
│           ├── config.properties    # App config
│           ├── features/
│           │   └── Checkout.feature # Checkout test scenarios
│           ├── testng-smoke.xml     # Smoke TestNG suite
│           └── testng-regression.xml # Regression TestNG suite
```

---

## 🚀 How to Run Tests

### Prerequisites
- Java 21
- Maven 3.9+
- Chrome browser

### Local Execution (Terminal)

```bash
# Smoke Tests
mvn test -Dcucumber.filter.tags="@Smoke"

# Regression Tests
mvn test -Dcucumber.filter.tags="@Regression"

# All Tests
mvn test
```

### VS Code — Run & Debug

1. Press `Cmd+Shift+D` (Run & Debug panel)
2. Select from dropdown:
   - 🔥 **Smoke Tests**
   - 🧪 **Regression Tests**
   - ▶️ **All Tests**
3. Click ▶️ or press `F5`

### HyperExecute (Cloud)

```bash
./hyperexecute --user "YOUR_USERNAME" --key "YOUR_KEY" --config hyperexecute.yaml
```

---

## 🔄 CI/CD Pipelines

### GitHub Actions

| Pipeline | Trigger | File |
|----------|---------|------|
| 🔥 CI - Smoke | Push/PR to `Devlop`/`main` | `.github/workflows/ci-smoke.yml` |
| 🧪 CD - Regression | Push to `main` + manual | `.github/workflows/cd-regression.yml` |
| 🚀 HyperExecute | Manual + nightly Mon-Fri | `.github/workflows/hyperexecute.yml` |

### Jenkins

1. Open http://localhost:8080
2. Run `WellsFargo-Automation` → **Build with Parameters**
3. Select: Smoke / Regression / All / HyperExecute

### CI/CD Flow

```
Code Push → 🔥 Smoke (auto) → Merge to main → 🧪 Regression (auto)
                                                      ↓
                                              🚀 HyperExecute (nightly)
                                                      ↓
                                              📊 Reports + 📧 Email
```

---

## 📊 Reports

| Report | Location |
|--------|----------|
| Extent HTML | `src/test/resources/reports/extent.html` |
| Cucumber HTML | `src/test/resources/reports/cucumber.html` |
| QMetry Summary | `src/test/resources/reports/qmetry-summary.html` |
| QMetry JSON | `src/test/resources/reports/qmetry-results.json` |
| QMetry HyperExecute | `src/test/resources/reports/qmetry-hyperexecute.html` |

---

## 🏷️ Test Tags

| Tag | Purpose | Scenarios |
|-----|---------|-----------|
| `@Smoke` | Critical path tests | Checkout flow |
| `@Regression` | Full test suite | All scenarios |

---

## 📦 Git Commands

```bash
git add -A                                    # Stage all changes
git commit -m "your message"                  # Commit
git push origin Devlop                        # Push to remote
```

---

## 🔐 Environment Setup

### GitHub Secrets (for Actions)
Add at: `Settings → Secrets → Actions`

| Secret | Value |
|--------|-------|
| `LT_USERNAME` | LambdaTest username |
| `LT_ACCESS_KEY` | LambdaTest access key |

### Jenkins Credentials
Add at: `Manage Jenkins → Credentials → Global`

| ID | Type | Value |
|----|------|-------|
| `lt-username` | Secret text | LambdaTest username |
| `lt-access-key` | Secret text | LambdaTest access key |

---

## 👤 Author

**Sathish Oruganti** — [GitHub](https://github.com/sathisho)
