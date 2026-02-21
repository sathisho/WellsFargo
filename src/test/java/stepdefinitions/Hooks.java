package stepdefinitions;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import org.openqa.selenium.WebDriver;
import utils.DriverFactory;
import utils.Log;

public class Hooks {
    @Before
    public void setUp() {
        WebDriver driver = DriverFactory.getDriver();
        Log.info("Driver initialized");
    }

    @After
    public void tearDown() {
        DriverFactory.quitDriver();
        Log.info("Driver quit");
    }
}
