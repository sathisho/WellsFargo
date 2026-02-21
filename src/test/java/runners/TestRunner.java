package runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import utils.ExtentManager;

@CucumberOptions(
        features = "src/test/resources/features",
        glue = {"stepdefinitions"},
        plugin = {"pretty", "html:src/test/resources/reports/cucumber.html"},
        monochrome = true
)
public class TestRunner extends AbstractTestNGCucumberTests {
    private static ExtentReports extent;
    private static ExtentTest test;

    @BeforeClass
    public static void setupReport() {
        extent = ExtentManager.getInstance();
        test = extent.createTest("Cucumber Test Execution");
    }

    @AfterClass
    public static void tearDownReport() {
        if (extent != null) {
            extent.flush();
        }
    }
}
