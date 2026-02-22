package utils;

import config.ConfigReader;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

public class DriverFactory {
    private static ThreadLocal<WebDriver> driver = new ThreadLocal<>();

    public static WebDriver getDriver() {
        if (driver.get() == null) {
            String executionMode = System.getProperty("execution.mode", "local");

            if ("remote".equalsIgnoreCase(executionMode)) {
                driver.set(createRemoteDriver());
            } else {
                driver.set(createLocalDriver());
            }
        }
        return driver.get();
    }

    private static WebDriver createLocalDriver() {
        String browser = ConfigReader.getProperty("browser");
        if (browser == null) browser = "chrome";
        switch (browser.toLowerCase()) {
            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                return new FirefoxDriver();
            case "chrome":
            default:
                WebDriverManager.chromedriver().setup();
                return new ChromeDriver();
        }
    }

    private static WebDriver createRemoteDriver() {
        String username = System.getenv("LT_USERNAME");
        String accessKey = System.getenv("LT_ACCESS_KEY");
        String gridURL = "@hub.lambdatest.com/wd/hub";

        String browser = System.getProperty("browser", "chrome");
        String browserVersion = System.getProperty("browserVersion", "latest");
        String os = System.getProperty("os", "Windows 11");

        ChromeOptions options = new ChromeOptions();
        options.setPlatformName(os);
        options.setBrowserVersion(browserVersion);

        HashMap<String, Object> ltOptions = new HashMap<>();
        ltOptions.put("username", username);
        ltOptions.put("accessKey", accessKey);
        ltOptions.put("project", "WellsFargo Automation");
        ltOptions.put("build", System.getProperty("buildName", "HyperExecute Build"));
        ltOptions.put("name", "Login Test");
        ltOptions.put("w3c", true);
        ltOptions.put("plugin", "java-testNG");
        options.setCapability("LT:Options", ltOptions);

        try {
            return new RemoteWebDriver(
                    new URL("https://" + username + ":" + accessKey + gridURL), options);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid LambdaTest grid URL", e);
        }
    }

    public static void quitDriver() {
        if (driver.get() != null) {
            driver.get().quit();
            driver.remove();
        }
    }
}
