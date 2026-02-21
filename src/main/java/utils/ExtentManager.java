package utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

public class ExtentManager {
    private static ExtentReports extent;

    public static ExtentReports getInstance() {
        if (extent == null) {
            ExtentSparkReporter sparkReporter = new ExtentSparkReporter("src/test/resources/reports/extent.html");
            sparkReporter.config().setTheme(Theme.STANDARD);
            sparkReporter.config().setDocumentTitle("Automation Report");
            sparkReporter.config().setReportName("Test Execution Report");
            extent = new ExtentReports();
            extent.attachReporter(sparkReporter);
        }
        return extent;
    }
}
