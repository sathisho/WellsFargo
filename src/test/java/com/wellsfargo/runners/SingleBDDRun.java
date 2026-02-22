package com.wellsfargo.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import com.wellsfargo.utils.TestResultCollector;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Parameters;

/**
 * SingleBDDRun — Enterprise QAF-compatible Cucumber runner.
 *
 * Execution modes:
 *   Default:    mvn clean test
 *   By tag:     mvn clean test -Dcucumber.filter.tags="@Smoke"
 *   By env:     mvn clean test -Denv=SIT
 *   Full:       mvn clean test -Dcucumber.filter.tags="@Regression" -Denv=UAT
 */
@CucumberOptions(
        features = {
                "src/test/resources/features",
                "scenarios"
        },
        glue = {
                "com.wellsfargo.steps"
        },
        plugin = {
                "pretty",
                "html:test-results/cucumber.html",
                "json:test-results/cucumber-report.json",
                "timeline:test-results/timeline",
                "usage:test-results/cucumber-usage.json"
        },
        monochrome = true,
        tags = ""
)
public class SingleBDDRun extends AbstractTestNGCucumberTests {

    @BeforeSuite
    public void beforeSuite() {
        String env = System.getProperty("env", "SIT");
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║  WellsFargo Enterprise Automation        ║");
        System.out.println("║  Environment: " + env + "                          ║");
        System.out.println("║  Runner: SingleBDDRun                    ║");
        System.out.println("╚══════════════════════════════════════════╝");
        TestResultCollector.startSuite();
    }

    @AfterSuite
    public void afterSuite() {
        TestResultCollector.finishSuite();
    }
}
