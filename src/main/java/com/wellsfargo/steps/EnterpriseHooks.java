package com.wellsfargo.steps;

import com.wellsfargo.utils.TestResultCollector;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import utils.DriverFactory;
import utils.Log;

/**
 * Enterprise Hooks — Cucumber Before/After for the com.wellsfargo.steps glue package.
 * Handles driver lifecycle, screenshot capture, and TestResultCollector integration.
 */
public class EnterpriseHooks {

    private long scenarioStartTime;

    @Before
    public void setUp(Scenario scenario) {
        scenarioStartTime = System.currentTimeMillis();
        WebDriver driver = DriverFactory.getDriver();
        Log.info("╔══════════════════════════════════════════╗");
        Log.info("║  Starting: " + scenario.getName());
        Log.info("║  Tags: " + scenario.getSourceTagNames());
        Log.info("╚══════════════════════════════════════════╝");
    }

    @After
    public void tearDown(Scenario scenario) {
        WebDriver driver = DriverFactory.getDriver();
        long duration = System.currentTimeMillis() - scenarioStartTime;

        // Capture screenshot on failure
        if (scenario.isFailed() && driver instanceof TakesScreenshot) {
            try {
                byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                scenario.attach(screenshot, "image/png", "failure-" + scenario.getName());
                Log.error("❌ Scenario FAILED: " + scenario.getName());
            } catch (Exception e) {
                Log.warn("Could not capture screenshot: " + e.getMessage());
            }
        }

        // Record result to dashboard JSON
        String featureName = scenario.getUri() != null
                ? scenario.getUri().toString().replaceAll(".*/(.*)\\.feature", "$1")
                : "Unknown";
        String status = scenario.isFailed() ? "FAIL" : "PASS";
        String error = scenario.isFailed() ? "Scenario failed — see screenshot" : null;
        TestResultCollector.recordScenario(featureName, scenario.getName(), status, duration, error);

        Log.info("Scenario Completed: " + scenario.getName() + " | Status: " + scenario.getStatus());
        DriverFactory.quitDriver();
        Log.info("Driver quit");
    }
}
