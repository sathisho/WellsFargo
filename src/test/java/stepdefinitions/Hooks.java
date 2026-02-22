package stepdefinitions;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import utils.DriverFactory;
import utils.Log;

public class Hooks {
    @Before
    public void setUp(Scenario scenario) {
        WebDriver driver = DriverFactory.getDriver();
        Log.info("========================================");
        Log.info("Starting Scenario: " + scenario.getName());
        Log.info("Tags: " + scenario.getSourceTagNames());
        Log.info("========================================");
    }

    @After
    public void tearDown(Scenario scenario) {
        WebDriver driver = DriverFactory.getDriver();

        // Capture screenshot on failure for QMetry/Extent reports
        if (scenario.isFailed() && driver instanceof TakesScreenshot) {
            try {
                byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                scenario.attach(screenshot, "image/png", "failure-screenshot-" + scenario.getName());
                Log.error("Scenario FAILED: " + scenario.getName());
            } catch (Exception e) {
                Log.warn("Could not capture screenshot: " + e.getMessage());
            }
        }

        Log.info("Scenario Completed: " + scenario.getName() + " | Status: " + scenario.getStatus());
        DriverFactory.quitDriver();
        Log.info("Driver quit");
    }
}
