package utils;

import net.masterthought.cucumber.Configuration;
import net.masterthought.cucumber.ReportBuilder;
import net.masterthought.cucumber.Reportable;
import net.masterthought.cucumber.json.support.Status;
import net.masterthought.cucumber.presentation.PresentationMode;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.activation.DataHandler;
import jakarta.activation.FileDataSource;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * QMetry Report Manager - Generates comprehensive QMetry-style test execution reports
 * with full HyperExecute integration.
 * 
 * Produces:
 * 1. Masterthought Cucumber HTML Dashboard (detailed interactive reports)
 * 2. QMetry Summary HTML Report (executive summary with charts)
 * 3. QMetry JSON Report (machine-readable test results for CI/CD integration)
 * 4. QMetry HyperExecute Integrated Report (unified report with links to all dashboards)
 */
public class QMetryReportManager {

    private static final String CUCUMBER_JSON_PATH = "src/test/resources/reports/cucumber-report.json";
    private static final String QMETRY_OUTPUT_DIR = "target/qmetry-reports";
    private static final String QMETRY_SUMMARY_REPORT = "src/test/resources/reports/qmetry-summary.html";
    private static final String QMETRY_JSON_REPORT = "src/test/resources/reports/qmetry-results.json";
    private static final String QMETRY_HE_REPORT = "src/test/resources/reports/qmetry-hyperexecute.html";
    private static final String PROJECT_NAME = "WellsFargo Automation";

    // HyperExecute context
    private static String buildName;
    private static String executionMode;
    private static String jobLabel;

    /**
     * Main method to generate all QMetry reports.
     */
    public static void generateQMetryReport() {
        Log.info("========================================");
        Log.info("  QMetry Report Generation Started");
        Log.info("========================================");

        // Capture HyperExecute context
        buildName = System.getProperty("buildName", System.getenv().getOrDefault("buildName", "Local Build"));
        executionMode = System.getProperty("execution.mode", "local");
        jobLabel = System.getenv().getOrDefault("JOB_LABEL", "wellsfargo-automation");

        try {
            // Wait for Cucumber JSON to be fully written (flush delay)
            waitForCucumberJson();

            Reportable masterthoughtResult = generateMasterthoughtReport();
            ReportSummary summary = generateQMetrySummaryReport();
            generateQMetryJsonReport(summary);
            generateHyperExecuteIntegratedReport(masterthoughtResult, summary);
            Log.info("========================================");
            Log.info("  QMetry Reports Generated Successfully");
            Log.info("========================================");
            Log.info("  Reports Location:");
            Log.info("    - Masterthought Dashboard: " + QMETRY_OUTPUT_DIR + "/cucumber-html-reports/");
            Log.info("    - QMetry Summary:          " + QMETRY_SUMMARY_REPORT);
            Log.info("    - QMetry JSON:             " + QMETRY_JSON_REPORT);
            Log.info("    - HyperExecute Report:     " + QMETRY_HE_REPORT);

            // Send email with reports
            if (summary != null) {
                sendEmailReport(summary);
            } else {
                Log.warn("Skipping email - no summary data available");
            }
        } catch (Exception e) {
            Log.error("Failed to generate QMetry reports: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Wait for the Cucumber JSON file to be fully written and valid.
     */
    private static void waitForCucumberJson() {
        File cucumberJson = new File(CUCUMBER_JSON_PATH);
        int maxRetries = 10;
        for (int i = 0; i < maxRetries; i++) {
            if (cucumberJson.exists() && cucumberJson.length() > 10) {
                try {
                    // Verify it's valid JSON
                    JsonParser.parseReader(new FileReader(cucumberJson)).getAsJsonArray();
                    Log.info("Cucumber JSON ready (" + cucumberJson.length() + " bytes)");
                    return;
                } catch (Exception e) {
                    Log.info("Cucumber JSON not ready yet, waiting... (attempt " + (i + 1) + ")");
                }
            }
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        }
        Log.warn("Cucumber JSON may not be fully written after " + maxRetries + " retries");
    }

    /**
     * Generate Masterthought Cucumber HTML Dashboard Report.
     */
    private static Reportable generateMasterthoughtReport() {
        File cucumberJson = new File(CUCUMBER_JSON_PATH);
        if (!cucumberJson.exists()) {
            Log.warn("Cucumber JSON report not found at: " + CUCUMBER_JSON_PATH);
            return null;
        }

        File outputDir = new File(QMETRY_OUTPUT_DIR);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        Configuration config = new Configuration(outputDir, PROJECT_NAME);
        config.addClassifications("Platform", System.getProperty("os.name"));
        config.addClassifications("Browser", System.getProperty("browser", "chrome"));
        config.addClassifications("Execution Mode", executionMode);
        config.addClassifications("Java Version", System.getProperty("java.version"));
        config.addClassifications("Environment", "QA");
        config.addClassifications("Build", buildName);
        config.addClassifications("Report Generated",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        if ("remote".equalsIgnoreCase(executionMode)) {
            config.addClassifications("Cloud Platform", "LambdaTest HyperExecute");
            config.addClassifications("Job Label", jobLabel);
        }

        config.addPresentationModes(PresentationMode.EXPAND_ALL_STEPS);
        config.setNotFailingStatuses(Collections.singleton(Status.SKIPPED));

        List<String> jsonFiles = new ArrayList<>();
        jsonFiles.add(cucumberJson.getAbsolutePath());

        ReportBuilder reportBuilder = new ReportBuilder(jsonFiles, config);
        Reportable result = reportBuilder.generateReports();

        if (result != null) {
            Log.info("QMetry Masterthought Report generated at: " + QMETRY_OUTPUT_DIR);
            Log.info("  Scenarios: Passed=" + result.getPassedScenarios() +
                     ", Failed=" + result.getFailedScenarios());
            Log.info("  Steps    : Passed=" + result.getPassedSteps() +
                     ", Failed=" + result.getFailedSteps() +
                     ", Skipped=" + result.getSkippedSteps());
        }
        return result;
    }

    /**
     * Generate QMetry Summary HTML Report - Executive summary with metrics.
     */
    private static ReportSummary generateQMetrySummaryReport() {
        File cucumberJson = new File(CUCUMBER_JSON_PATH);
        if (!cucumberJson.exists()) {
            Log.warn("Cucumber JSON not found, skipping QMetry summary report");
            return null;
        }

        try {
            JsonArray features = JsonParser.parseReader(new FileReader(cucumberJson)).getAsJsonArray();

            int totalScenarios = 0, passedScenarios = 0, failedScenarios = 0, skippedScenarios = 0;
            int totalSteps = 0, passedSteps = 0, failedSteps = 0, skippedSteps = 0;
            long totalDurationNanos = 0;
            List<ScenarioResult> scenarioResults = new ArrayList<>();
            int featureCount = 0;

            for (JsonElement featureElement : features) {
                JsonObject feature = featureElement.getAsJsonObject();
                String featureName = feature.get("name").getAsString();
                featureCount++;
                JsonArray elements = feature.has("elements") ? feature.getAsJsonArray("elements") : new JsonArray();

                for (JsonElement elementElement : elements) {
                    JsonObject element = elementElement.getAsJsonObject();
                    if (!"scenario".equals(element.get("type").getAsString())) continue;

                    totalScenarios++;
                    String scenarioName = element.get("name").getAsString();
                    boolean scenarioPassed = true;
                    boolean scenarioSkipped = false;
                    int scenarioSteps = 0;
                    int scenarioPassedSteps = 0;
                    long scenarioDuration = 0;
                    StringBuilder failureMessage = new StringBuilder();
                    List<String> tags = new ArrayList<>();

                    // Extract tags
                    if (element.has("tags")) {
                        for (JsonElement tagEl : element.getAsJsonArray("tags")) {
                            tags.add(tagEl.getAsJsonObject().get("name").getAsString());
                        }
                    }

                    JsonArray steps = element.has("steps") ? element.getAsJsonArray("steps") : new JsonArray();
                    for (JsonElement stepElement : steps) {
                        JsonObject step = stepElement.getAsJsonObject();
                        JsonObject result = step.has("result") ? step.getAsJsonObject("result") : new JsonObject();
                        String status = result.has("status") ? result.get("status").getAsString() : "undefined";
                        long duration = result.has("duration") ? result.get("duration").getAsLong() : 0;

                        totalSteps++;
                        scenarioSteps++;
                        scenarioDuration += duration;
                        totalDurationNanos += duration;

                        switch (status) {
                            case "passed":
                                passedSteps++;
                                scenarioPassedSteps++;
                                break;
                            case "failed":
                                failedSteps++;
                                scenarioPassed = false;
                                String errorMsg = result.has("error_message") ?
                                        result.get("error_message").getAsString() : "Unknown error";
                                failureMessage.append(step.get("keyword").getAsString())
                                        .append(step.get("name").getAsString())
                                        .append(": ").append(errorMsg.split("\n")[0]).append("<br>");
                                break;
                            case "skipped":
                                skippedSteps++;
                                scenarioSkipped = true;
                                break;
                            default:
                                skippedSteps++;
                                break;
                        }
                    }

                    String scenarioStatus;
                    if (!scenarioPassed) {
                        failedScenarios++;
                        scenarioStatus = "FAIL";
                    } else if (scenarioSkipped && scenarioPassedSteps == 0) {
                        skippedScenarios++;
                        scenarioStatus = "SKIP";
                    } else {
                        passedScenarios++;
                        scenarioStatus = "PASS";
                    }

                    scenarioResults.add(new ScenarioResult(
                            featureName, scenarioName, scenarioStatus,
                            scenarioSteps, scenarioPassedSteps,
                            scenarioDuration, failureMessage.toString(), tags
                    ));
                }
            }

            double totalDurationSec = totalDurationNanos / 1_000_000_000.0;
            double passRate = totalScenarios > 0 ? (passedScenarios * 100.0 / totalScenarios) : 0;

            ReportSummary summary = new ReportSummary(
                    featureCount, totalScenarios, passedScenarios, failedScenarios, skippedScenarios,
                    totalSteps, passedSteps, failedSteps, skippedSteps,
                    totalDurationNanos, totalDurationSec, passRate, scenarioResults
            );

            // Generate the HTML report
            File reportFile = new File(QMETRY_SUMMARY_REPORT);
            reportFile.getParentFile().mkdirs();
            try (PrintWriter writer = new PrintWriter(new FileWriter(reportFile))) {
                writer.println(buildQMetryHtml(summary));
            }
            Log.info("QMetry Summary Report generated at: " + QMETRY_SUMMARY_REPORT);
            return summary;

        } catch (Exception e) {
            Log.error("Failed to generate QMetry summary report: " + e.getMessage());
            return null;
        }
    }

    /**
     * Generate QMetry JSON report for CI/CD integration.
     */
    private static void generateQMetryJsonReport(ReportSummary summary) {
        File cucumberJson = new File(CUCUMBER_JSON_PATH);
        if (!cucumberJson.exists()) {
            Log.warn("Cucumber JSON not found, skipping QMetry JSON report");
            return;
        }

        try {
            JsonObject qmetryReport = new JsonObject();

            // Report metadata
            JsonObject metadata = new JsonObject();
            metadata.addProperty("projectName", PROJECT_NAME);
            metadata.addProperty("reportType", "QMetry Test Execution Report");
            metadata.addProperty("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            metadata.addProperty("environment", "QA");
            metadata.addProperty("platform", System.getProperty("os.name"));
            metadata.addProperty("javaVersion", System.getProperty("java.version"));
            metadata.addProperty("browser", System.getProperty("browser", "chrome"));
            metadata.addProperty("executionMode", executionMode);
            metadata.addProperty("buildName", buildName);

            // HyperExecute metadata
            if ("remote".equalsIgnoreCase(executionMode)) {
                JsonObject hyperExecute = new JsonObject();
                hyperExecute.addProperty("platform", "LambdaTest HyperExecute");
                hyperExecute.addProperty("jobLabel", jobLabel);
                hyperExecute.addProperty("buildName", buildName);
                metadata.add("hyperExecute", hyperExecute);
            }
            qmetryReport.add("metadata", metadata);

            // Process test results from summary
            JsonArray testCases = new JsonArray();
            int totalPass = 0, totalFail = 0, totalSkip = 0;
            long totalDuration = 0;

            if (summary != null && summary.scenarioResults != null) {
                for (int i = 0; i < summary.scenarioResults.size(); i++) {
                    ScenarioResult sr = summary.scenarioResults.get(i);
                    JsonObject testCase = new JsonObject();
                    testCase.addProperty("testCaseId", "TC-" + String.format("%03d", i + 1));
                    testCase.addProperty("featureName", sr.featureName);
                    testCase.addProperty("scenarioName", sr.scenarioName);
                    testCase.addProperty("status", sr.status);
                    testCase.addProperty("totalSteps", sr.totalSteps);
                    testCase.addProperty("passedSteps", sr.passedSteps);
                    testCase.addProperty("durationMs", (long) (sr.durationNanos / 1_000_000));
                    testCase.addProperty("durationFormatted", formatDuration((long) sr.durationNanos));

                    if (!sr.tags.isEmpty()) {
                        JsonArray tagsArray = new JsonArray();
                        sr.tags.forEach(tagsArray::add);
                        testCase.add("tags", tagsArray);
                    }

                    if (!sr.failureMessage.isEmpty()) {
                        testCase.addProperty("failureMessage", sr.failureMessage.replace("<br>", "\n"));
                    }

                    testCases.add(testCase);

                    switch (sr.status) {
                        case "PASS": totalPass++; break;
                        case "FAIL": totalFail++; break;
                        default: totalSkip++; break;
                    }
                    totalDuration += (long) sr.durationNanos;
                }
            }

            qmetryReport.add("testCases", testCases);

            // Summary
            JsonObject summaryJson = new JsonObject();
            summaryJson.addProperty("totalTestCases", testCases.size());
            summaryJson.addProperty("passed", totalPass);
            summaryJson.addProperty("failed", totalFail);
            summaryJson.addProperty("skipped", totalSkip);
            summaryJson.addProperty("passRate", testCases.size() > 0 ?
                    Math.round(totalPass * 100.0 / testCases.size() * 100.0) / 100.0 : 0);
            summaryJson.addProperty("totalDurationMs", totalDuration / 1_000_000);
            summaryJson.addProperty("totalDurationFormatted", formatDuration(totalDuration));
            summaryJson.addProperty("result", totalFail == 0 ? "PASS" : "FAIL");

            // Report file paths for cross-referencing
            JsonObject reportFiles = new JsonObject();
            reportFiles.addProperty("qmetrySummary", QMETRY_SUMMARY_REPORT);
            reportFiles.addProperty("qmetryJson", QMETRY_JSON_REPORT);
            reportFiles.addProperty("hyperExecuteReport", QMETRY_HE_REPORT);
            reportFiles.addProperty("masterthoughtDashboard", QMETRY_OUTPUT_DIR + "/cucumber-html-reports/overview-features.html");
            reportFiles.addProperty("extentReport", "src/test/resources/reports/extent.html");
            reportFiles.addProperty("cucumberReport", "src/test/resources/reports/cucumber.html");
            summaryJson.add("reportFiles", reportFiles);

            qmetryReport.add("summary", summaryJson);

            // Write JSON report
            File jsonReportFile = new File(QMETRY_JSON_REPORT);
            jsonReportFile.getParentFile().mkdirs();
            try (FileWriter fw = new FileWriter(jsonReportFile)) {
                new GsonBuilder().setPrettyPrinting().create().toJson(qmetryReport, fw);
            }
            Log.info("QMetry JSON Report generated at: " + QMETRY_JSON_REPORT);

        } catch (Exception e) {
            Log.error("Failed to generate QMetry JSON report: " + e.getMessage());
        }
    }

    /**
     * Generate HyperExecute-integrated QMetry report - combines all report data
     * with direct links to HyperExecute artifacts.
     */
    private static void generateHyperExecuteIntegratedReport(Reportable masterthoughtResult, ReportSummary summary) {
        if (summary == null) {
            Log.warn("No summary data available, skipping HyperExecute integrated report");
            return;
        }

        try {
            File reportFile = new File(QMETRY_HE_REPORT);
            reportFile.getParentFile().mkdirs();
            try (PrintWriter writer = new PrintWriter(new FileWriter(reportFile))) {
                writer.println(buildHyperExecuteReportHtml(masterthoughtResult, summary));
            }
            Log.info("QMetry HyperExecute Integrated Report generated at: " + QMETRY_HE_REPORT);
        } catch (Exception e) {
            Log.error("Failed to generate HyperExecute integrated report: " + e.getMessage());
        }
    }

    /**
     * Build the HyperExecute Integrated Report HTML.
     */
    private static String buildHyperExecuteReportHtml(Reportable masterthoughtResult, ReportSummary summary) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String overallStatus = summary.failedScenarios == 0 ? "PASS" : "FAIL";
        String statusColor = summary.failedScenarios == 0 ? "#28a745" : "#dc3545";
        boolean isRemote = "remote".equalsIgnoreCase(executionMode);

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n");
        html.append("<meta charset=\"UTF-8\">\n<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("<title>QMetry + HyperExecute Report - ").append(PROJECT_NAME).append("</title>\n");
        html.append("<style>\n");
        html.append(getHyperExecuteReportCSS());
        html.append("</style>\n</head>\n<body>\n");

        // ===== HEADER =====
        html.append("<div class=\"header\">\n");
        html.append("  <div class=\"header-content\">\n");
        html.append("    <div class=\"header-left\">\n");
        html.append("      <h1>&#x1F680; QMetry + HyperExecute Report</h1>\n");
        html.append("      <p class=\"subtitle\">").append(PROJECT_NAME);
        if (isRemote) {
            html.append(" | LambdaTest HyperExecute Cloud");
        }
        html.append("</p>\n");
        html.append("      <p class=\"timestamp\">Generated: ").append(timestamp).append("</p>\n");
        html.append("    </div>\n");
        html.append("    <div class=\"header-right\">\n");
        html.append("      <div class=\"overall-badge\" style=\"background:").append(statusColor).append("\">\n");
        html.append("        ").append(overallStatus).append("\n");
        html.append("      </div>\n");
        html.append("      <div class=\"pass-rate-circle\">\n");
        html.append("        <span>").append(String.format("%.0f%%", summary.passRate)).append("</span>\n");
        html.append("      </div>\n");
        html.append("    </div>\n");
        html.append("  </div>\n");
        html.append("</div>\n\n");

        // ===== NAVIGATION TABS =====
        html.append("<div class=\"container\">\n");
        html.append("<div class=\"report-nav\">\n");
        html.append("  <h3>&#x1F4C1; Available Reports</h3>\n");
        html.append("  <div class=\"nav-cards\">\n");
        html.append("    <a class=\"nav-card active\" href=\"#overview\"><span class=\"nav-icon\">&#x1F4CA;</span>Overview</a>\n");
        html.append("    <a class=\"nav-card\" href=\"qmetry-summary.html\"><span class=\"nav-icon\">&#x1F4DD;</span>QMetry Summary</a>\n");
        html.append("    <a class=\"nav-card\" href=\"extent.html\"><span class=\"nav-icon\">&#x1F4D1;</span>Extent Report</a>\n");
        html.append("    <a class=\"nav-card\" href=\"cucumber.html\"><span class=\"nav-icon\">&#x1F952;</span>Cucumber Report</a>\n");
        html.append("    <a class=\"nav-card\" href=\"qmetry-results.json\" target=\"_blank\"><span class=\"nav-icon\">&#x1F4BE;</span>JSON Export</a>\n");
        html.append("  </div>\n");
        html.append("</div>\n\n");

        // ===== EXECUTION ENVIRONMENT =====
        html.append("<div class=\"section\" id=\"overview\">\n");
        html.append("  <h2>&#x2699;&#xFE0F; Execution Environment</h2>\n");
        html.append("  <div class=\"env-grid\">\n");
        addEnvItem(html, "Platform", System.getProperty("os.name"), "&#x1F4BB;");
        addEnvItem(html, "Java", System.getProperty("java.version"), "&#x2615;");
        addEnvItem(html, "Browser", System.getProperty("browser", "Chrome"), "&#x1F310;");
        addEnvItem(html, "Mode", isRemote ? "HyperExecute Cloud" : "Local", "&#x2601;&#xFE0F;");
        addEnvItem(html, "Build", buildName, "&#x1F3D7;&#xFE0F;");
        addEnvItem(html, "Duration", String.format("%.2fs", summary.totalDurationSec), "&#x23F1;&#xFE0F;");
        addEnvItem(html, "Features", String.valueOf(summary.featureCount), "&#x1F4C4;");
        addEnvItem(html, "Framework", "Cucumber + TestNG + Selenium", "&#x1F527;");
        html.append("  </div>\n");
        html.append("</div>\n\n");

        // ===== KPI DASHBOARD =====
        html.append("<div class=\"section\">\n");
        html.append("  <h2>&#x1F4C8; Test Execution Dashboard</h2>\n");
        html.append("  <div class=\"kpi-grid\">\n");
        addKpiCard(html, "Total Scenarios", String.valueOf(summary.totalScenarios), "total", "&#x1F4CB;");
        addKpiCard(html, "Passed", String.valueOf(summary.passedScenarios), "pass", "&#x2705;");
        addKpiCard(html, "Failed", String.valueOf(summary.failedScenarios), "fail", "&#x274C;");
        addKpiCard(html, "Skipped", String.valueOf(summary.skippedScenarios), "skip", "&#x23ED;&#xFE0F;");
        addKpiCard(html, "Pass Rate", String.format("%.1f%%", summary.passRate), "rate", "&#x1F3AF;");
        addKpiCard(html, "Total Steps", String.valueOf(summary.totalSteps), "total", "&#x1F463;");
        html.append("  </div>\n");
        html.append("</div>\n\n");

        // ===== VISUAL CHARTS =====
        html.append("<div class=\"section\">\n");
        html.append("  <h2>&#x1F4CA; Visual Analytics</h2>\n");
        html.append("  <div class=\"charts-grid\">\n");

        // Scenario donut
        buildDonutChart(html, "Scenario Results", summary.totalScenarios,
                summary.passedScenarios, summary.failedScenarios, summary.skippedScenarios, summary.passRate);

        // Steps donut
        double stepPassRate = summary.totalSteps > 0 ? (summary.passedSteps * 100.0 / summary.totalSteps) : 0;
        buildDonutChart(html, "Step Results", summary.totalSteps,
                summary.passedSteps, summary.failedSteps, summary.skippedSteps, stepPassRate);

        // Progress bar
        html.append("    <div class=\"chart-card\">\n");
        html.append("      <h3>Execution Progress</h3>\n");
        html.append("      <div class=\"progress-bars\">\n");
        addProgressBar(html, "Scenarios", summary.passedScenarios, summary.failedScenarios, summary.skippedScenarios, summary.totalScenarios);
        addProgressBar(html, "Steps", summary.passedSteps, summary.failedSteps, summary.skippedSteps, summary.totalSteps);
        html.append("      </div>\n");
        html.append("      <div class=\"execution-meta\">\n");
        html.append("        <div class=\"meta-item\">&#x23F1;&#xFE0F; <strong>").append(String.format("%.2fs", summary.totalDurationSec)).append("</strong> total</div>\n");
        html.append("        <div class=\"meta-item\">&#x1F4C4; <strong>").append(summary.featureCount).append("</strong> feature(s)</div>\n");
        html.append("        <div class=\"meta-item\">&#x1F50D; <strong>").append(summary.totalScenarios).append("</strong> scenario(s)</div>\n");
        html.append("      </div>\n");
        html.append("    </div>\n");

        html.append("  </div>\n");
        html.append("</div>\n\n");

        // ===== SCENARIO DETAILS TABLE =====
        html.append("<div class=\"section\">\n");
        html.append("  <h2>&#x1F4DD; Test Case Details</h2>\n");
        html.append("  <div class=\"table-controls\">\n");
        html.append("    <div class=\"filter-pills\">\n");
        html.append("      <button class=\"pill active\" onclick=\"filterTable('all')\">All (").append(summary.totalScenarios).append(")</button>\n");
        html.append("      <button class=\"pill pill-pass\" onclick=\"filterTable('PASS')\">&#x2705; Passed (").append(summary.passedScenarios).append(")</button>\n");
        html.append("      <button class=\"pill pill-fail\" onclick=\"filterTable('FAIL')\">&#x274C; Failed (").append(summary.failedScenarios).append(")</button>\n");
        if (summary.skippedScenarios > 0) {
            html.append("      <button class=\"pill pill-skip\" onclick=\"filterTable('SKIP')\">&#x23ED;&#xFE0F; Skipped (").append(summary.skippedScenarios).append(")</button>\n");
        }
        html.append("    </div>\n");
        html.append("  </div>\n");
        html.append("  <table class=\"results-table\" id=\"scenarioTable\">\n");
        html.append("    <thead>\n");
        html.append("      <tr><th>#</th><th>Test ID</th><th>Feature</th><th>Scenario</th><th>Steps</th><th>Duration</th><th>Status</th></tr>\n");
        html.append("    </thead>\n");
        html.append("    <tbody>\n");

        for (int i = 0; i < summary.scenarioResults.size(); i++) {
            ScenarioResult sr = summary.scenarioResults.get(i);
            String statusClass = switch (sr.status) {
                case "PASS" -> "status-pass";
                case "FAIL" -> "status-fail";
                default -> "status-skip";
            };
            String statusIcon = switch (sr.status) {
                case "PASS" -> "&#x2705;";
                case "FAIL" -> "&#x274C;";
                default -> "&#x23ED;&#xFE0F;";
            };
            String duration = formatDuration((long) sr.durationNanos);

            html.append("      <tr class=\"scenario-row\" data-status=\"").append(sr.status).append("\">\n");
            html.append("        <td>").append(i + 1).append("</td>\n");
            html.append("        <td><code>TC-").append(String.format("%03d", i + 1)).append("</code></td>\n");
            html.append("        <td>").append(sr.featureName).append("</td>\n");
            html.append("        <td>").append(sr.scenarioName);
            if (!sr.tags.isEmpty()) {
                html.append(" <span class=\"tag-list\">");
                for (String tag : sr.tags) {
                    html.append("<span class=\"tag\">").append(tag).append("</span>");
                }
                html.append("</span>");
            }
            html.append("</td>\n");
            html.append("        <td>").append(sr.passedSteps).append("/").append(sr.totalSteps).append("</td>\n");
            html.append("        <td>").append(duration).append("</td>\n");
            html.append("        <td><span class=\"status-badge ").append(statusClass).append("\">").append(statusIcon).append(" ").append(sr.status).append("</span></td>\n");
            html.append("      </tr>\n");

            if (!sr.failureMessage.isEmpty()) {
                html.append("      <tr class=\"failure-row\" data-status=\"").append(sr.status).append("\"><td colspan=\"7\"><div class=\"failure-detail\">&#x26A0;&#xFE0F; ")
                        .append(sr.failureMessage).append("</div></td></tr>\n");
            }
        }

        html.append("    </tbody>\n");
        html.append("  </table>\n");
        html.append("</div>\n\n");

        // ===== FOOTER =====
        html.append("<div class=\"footer\">\n");
        html.append("  <div class=\"footer-content\">\n");
        html.append("    <p><strong>QMetry + HyperExecute Integrated Report</strong></p>\n");
        html.append("    <p>").append(PROJECT_NAME).append(" | Cucumber + TestNG + Selenium");
        if (isRemote) {
            html.append(" | LambdaTest HyperExecute Cloud");
        }
        html.append("</p>\n");
        html.append("    <p>Report Time: ").append(timestamp).append(" | Build: ").append(buildName).append("</p>\n");
        html.append("  </div>\n");
        html.append("</div>\n\n");

        html.append("</div>\n");

        // ===== JAVASCRIPT =====
        html.append("<script>\n");
        html.append("function filterTable(status) {\n");
        html.append("  const rows = document.querySelectorAll('.scenario-row, .failure-row');\n");
        html.append("  const pills = document.querySelectorAll('.pill');\n");
        html.append("  pills.forEach(p => p.classList.remove('active'));\n");
        html.append("  event.target.classList.add('active');\n");
        html.append("  rows.forEach(row => {\n");
        html.append("    if (status === 'all' || row.dataset.status === status) {\n");
        html.append("      row.style.display = '';\n");
        html.append("    } else {\n");
        html.append("      row.style.display = 'none';\n");
        html.append("    }\n");
        html.append("  });\n");
        html.append("}\n");
        html.append("</script>\n");

        html.append("</body>\n</html>");
        return html.toString();
    }

    // ===== EMAIL METHODS =====

    /**
     * Send QMetry report via email with all report files attached.
     */
    private static void sendEmailReport(ReportSummary summary) {
        Properties emailConfig = loadEmailConfig();
        if (emailConfig == null) return;

        String enabled = emailConfig.getProperty("email.enabled", "false");
        if (!"true".equalsIgnoreCase(enabled)) {
            Log.info("Email reporting is disabled (email.enabled=false)");
            return;
        }

        String smtpHost = emailConfig.getProperty("email.smtp.host", "smtp.gmail.com");
        String smtpPort = emailConfig.getProperty("email.smtp.port", "587");
        String fromEmail = emailConfig.getProperty("email.from", "");
        String toEmail = emailConfig.getProperty("email.to", "");
        String password = emailConfig.getProperty("email.password", "");
        String subject = emailConfig.getProperty("email.subject", "QMetry Test Execution Report");

        if (fromEmail.isEmpty() || toEmail.isEmpty() || password.isEmpty()
                || "YOUR_APP_PASSWORD_HERE".equals(password)) {
            Log.warn("Email not configured properly. Set email.from, email.to, and email.password in config.properties");
            Log.warn("For Gmail: Generate App Password at https://myaccount.google.com/apppasswords");
            return;
        }

        try {
            Log.info("Sending QMetry report email to: " + toEmail);

            // SMTP properties
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", smtpHost);
            props.put("mail.smtp.port", smtpPort);
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");
            props.put("mail.smtp.ssl.trust", smtpHost);

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(fromEmail, password);
                }
            });

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail, PROJECT_NAME + " Automation"));

            // Support multiple recipients (comma-separated)
            String[] recipients = toEmail.split(",");
            for (String recipient : recipients) {
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient.trim()));
            }

            // Build subject with status
            String overallStatus = summary.failedScenarios == 0 ? "\u2705 PASSED" : "\u274C FAILED";
            String fullSubject = overallStatus + " | " + subject
                    + " | " + summary.passedScenarios + "/" + summary.totalScenarios + " Passed";
            message.setSubject(fullSubject);

            // Create multipart message (HTML body + attachments)
            Multipart multipart = new MimeMultipart();

            // HTML body
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(buildEmailHtml(summary), "text/html; charset=utf-8");
            multipart.addBodyPart(htmlPart);

            // Attach report files
            attachFile(multipart, QMETRY_SUMMARY_REPORT, "QMetry-Summary-Report.html");
            attachFile(multipart, QMETRY_HE_REPORT, "QMetry-HyperExecute-Report.html");
            attachFile(multipart, QMETRY_JSON_REPORT, "QMetry-Results.json");
            attachFile(multipart, "src/test/resources/reports/extent.html", "Extent-Report.html");
            attachFile(multipart, "src/test/resources/reports/cucumber.html", "Cucumber-Report.html");

            message.setContent(multipart);

            // Send
            Transport.send(message);
            Log.info("\u2709\uFE0F QMetry report email sent successfully to: " + toEmail);

        } catch (MessagingException e) {
            Log.error("Failed to send email report: " + e.getMessage());
            Log.error("Tip: For Gmail, ensure you're using an App Password (not your regular password)");
            e.printStackTrace();
        } catch (Exception e) {
            Log.error("Unexpected error sending email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load email configuration from config.properties.
     */
    private static Properties loadEmailConfig() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("src/test/resources/config.properties")) {
            props.load(fis);
            return props;
        } catch (IOException e) {
            Log.warn("Could not load config.properties for email settings: " + e.getMessage());
            return null;
        }
    }

    /**
     * Attach a file to the email multipart.
     */
    private static void attachFile(Multipart multipart, String filePath, String attachmentName) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                MimeBodyPart attachmentPart = new MimeBodyPart();
                FileDataSource source = new FileDataSource(file);
                attachmentPart.setDataHandler(new DataHandler(source));
                attachmentPart.setFileName(attachmentName);
                multipart.addBodyPart(attachmentPart);
                Log.info("  Attached: " + attachmentName + " (" + (file.length() / 1024) + " KB)");
            } else {
                Log.warn("  Skipped attachment (not found): " + filePath);
            }
        } catch (MessagingException e) {
            Log.warn("  Failed to attach " + attachmentName + ": " + e.getMessage());
        }
    }

    /**
     * Build the HTML email body with test execution summary.
     */
    private static String buildEmailHtml(ReportSummary s) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String overallStatus = s.failedScenarios == 0 ? "PASSED" : "FAILED";
        String statusColor = s.failedScenarios == 0 ? "#28a745" : "#dc3545";
        String statusBg = s.failedScenarios == 0 ? "#d4edda" : "#f8d7da";
        boolean isRemote = "remote".equalsIgnoreCase(executionMode);

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'></head>\n");
        html.append("<body style='margin:0;padding:0;font-family:Segoe UI,Tahoma,Geneva,Verdana,sans-serif;background:#f4f4f4;'>\n");

        // Container
        html.append("<div style='max-width:700px;margin:20px auto;background:white;border-radius:12px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.1);'>\n");

        // Header
        html.append("<div style='background:linear-gradient(135deg,#1a237e 0%,#283593 50%,#3949ab 100%);color:white;padding:30px 35px;'>\n");
        html.append("  <h1 style='margin:0;font-size:22px;font-weight:600;'>\u1F4CA QMetry Test Execution Report</h1>\n");
        html.append("  <p style='margin:6px 0 0;font-size:13px;opacity:0.85;'>").append(PROJECT_NAME);
        if (isRemote) html.append(" | LambdaTest HyperExecute Cloud");
        html.append("</p>\n");
        html.append("  <p style='margin:4px 0 0;font-size:12px;opacity:0.7;'>Generated: ").append(timestamp).append("</p>\n");
        html.append("</div>\n\n");

        // Status Banner
        html.append("<div style='background:").append(statusBg).append(";padding:20px 35px;text-align:center;border-bottom:3px solid ").append(statusColor).append(";'>\n");
        html.append("  <span style='font-size:32px;font-weight:700;color:").append(statusColor).append(";'>").append(overallStatus).append("</span>\n");
        html.append("  <p style='margin:6px 0 0;color:").append(statusColor).append(";font-size:15px;'>");
        html.append(s.passedScenarios).append(" of ").append(s.totalScenarios).append(" scenarios passed (");
        html.append(String.format("%.1f%%", s.passRate)).append(" pass rate)</p>\n");
        html.append("</div>\n\n");

        // KPI Summary
        html.append("<div style='padding:25px 35px;'>\n");
        html.append("  <h2 style='margin:0 0 16px;font-size:18px;color:#1a237e;'>Test Execution Summary</h2>\n");
        html.append("  <table style='width:100%;border-collapse:collapse;'>\n");
        html.append("    <tr>\n");
        addKpiCell(html, "Total", String.valueOf(s.totalScenarios), "#1a237e", "#e8eaf6");
        addKpiCell(html, "Passed", String.valueOf(s.passedScenarios), "#28a745", "#d4edda");
        addKpiCell(html, "Failed", String.valueOf(s.failedScenarios), "#dc3545", "#f8d7da");
        addKpiCell(html, "Skipped", String.valueOf(s.skippedScenarios), "#ffc107", "#fff3cd");
        html.append("    </tr>\n");
        html.append("  </table>\n\n");

        // Steps summary
        html.append("  <table style='width:100%;border-collapse:collapse;margin-top:12px;'>\n");
        html.append("    <tr>\n");
        addKpiCell(html, "Total Steps", String.valueOf(s.totalSteps), "#546e7a", "#eceff1");
        addKpiCell(html, "Steps Passed", String.valueOf(s.passedSteps), "#28a745", "#d4edda");
        addKpiCell(html, "Steps Failed", String.valueOf(s.failedSteps), "#dc3545", "#f8d7da");
        addKpiCell(html, "Duration", String.format("%.2fs", s.totalDurationSec), "#17a2b8", "#d1ecf1");
        html.append("    </tr>\n");
        html.append("  </table>\n");
        html.append("</div>\n\n");

        // Environment
        html.append("<div style='padding:0 35px 20px;'>\n");
        html.append("  <h2 style='margin:0 0 12px;font-size:18px;color:#1a237e;'>Execution Environment</h2>\n");
        html.append("  <table style='width:100%;border-collapse:collapse;font-size:13px;'>\n");
        addEnvRow(html, "Platform", System.getProperty("os.name"));
        addEnvRow(html, "Java", System.getProperty("java.version"));
        addEnvRow(html, "Browser", System.getProperty("browser", "Chrome"));
        addEnvRow(html, "Execution Mode", isRemote ? "HyperExecute Cloud" : "Local");
        addEnvRow(html, "Build", buildName);
        addEnvRow(html, "Features", String.valueOf(s.featureCount));
        html.append("  </table>\n");
        html.append("</div>\n\n");

        // Scenario Details Table
        html.append("<div style='padding:0 35px 25px;'>\n");
        html.append("  <h2 style='margin:0 0 12px;font-size:18px;color:#1a237e;'>Scenario Details</h2>\n");
        html.append("  <table style='width:100%;border-collapse:collapse;font-size:13px;'>\n");
        html.append("    <tr style='background:#1a237e;color:white;'>\n");
        html.append("      <th style='padding:10px 12px;text-align:left;'>#</th>\n");
        html.append("      <th style='padding:10px 12px;text-align:left;'>Scenario</th>\n");
        html.append("      <th style='padding:10px 12px;text-align:center;'>Steps</th>\n");
        html.append("      <th style='padding:10px 12px;text-align:center;'>Duration</th>\n");
        html.append("      <th style='padding:10px 12px;text-align:center;'>Status</th>\n");
        html.append("    </tr>\n");

        for (int i = 0; i < s.scenarioResults.size(); i++) {
            ScenarioResult sr = s.scenarioResults.get(i);
            String rowBg = i % 2 == 0 ? "#ffffff" : "#f8f9fa";
            String stColor = switch (sr.status) {
                case "PASS" -> "#28a745";
                case "FAIL" -> "#dc3545";
                default -> "#ffc107";
            };
            String stBg = switch (sr.status) {
                case "PASS" -> "#d4edda";
                case "FAIL" -> "#f8d7da";
                default -> "#fff3cd";
            };
            String stIcon = switch (sr.status) {
                case "PASS" -> "\u2705";
                case "FAIL" -> "\u274C";
                default -> "\u23ED";
            };

            html.append("    <tr style='background:").append(rowBg).append(";'>\n");
            html.append("      <td style='padding:10px 12px;'>").append(i + 1).append("</td>\n");
            html.append("      <td style='padding:10px 12px;'>").append(sr.scenarioName).append("</td>\n");
            html.append("      <td style='padding:10px 12px;text-align:center;'>").append(sr.passedSteps).append("/").append(sr.totalSteps).append("</td>\n");
            html.append("      <td style='padding:10px 12px;text-align:center;'>").append(formatDuration((long) sr.durationNanos)).append("</td>\n");
            html.append("      <td style='padding:10px 12px;text-align:center;'>\n");
            html.append("        <span style='display:inline-block;padding:3px 12px;border-radius:12px;font-weight:600;font-size:12px;background:").append(stBg).append(";color:").append(stColor).append(";'>")
                    .append(stIcon).append(" ").append(sr.status).append("</span>\n");
            html.append("      </td>\n");
            html.append("    </tr>\n");

            if (!sr.failureMessage.isEmpty()) {
                html.append("    <tr><td colspan='5' style='padding:8px 12px 12px 40px;background:#fff5f5;'>\n");
                html.append("      <div style='color:#721c24;font-size:12px;padding:8px;background:#f8d7da;border-radius:4px;border-left:3px solid #dc3545;'>")
                        .append(sr.failureMessage).append("</div>\n");
                html.append("    </td></tr>\n");
            }
        }
        html.append("  </table>\n");
        html.append("</div>\n\n");

        // Attachments note
        html.append("<div style='padding:15px 35px;background:#e8eaf6;border-top:1px solid #c5cae9;'>\n");
        html.append("  <p style='margin:0;font-size:13px;color:#1a237e;font-weight:600;'>\u1F4CE Attached Reports:</p>\n");
        html.append("  <ul style='margin:8px 0 0;padding-left:20px;font-size:12px;color:#333;'>\n");
        html.append("    <li>QMetry Summary Report (HTML)</li>\n");
        html.append("    <li>QMetry HyperExecute Integrated Report (HTML)</li>\n");
        html.append("    <li>QMetry Results (JSON)</li>\n");
        html.append("    <li>Extent Spark Report (HTML)</li>\n");
        html.append("    <li>Cucumber Report (HTML)</li>\n");
        html.append("  </ul>\n");
        html.append("</div>\n\n");

        // Footer
        html.append("<div style='padding:18px 35px;background:#f8f9fa;text-align:center;border-top:1px solid #dee2e6;'>\n");
        html.append("  <p style='margin:0;font-size:12px;color:#666;'>QMetry Test Execution Report | ").append(PROJECT_NAME).append("</p>\n");
        html.append("  <p style='margin:4px 0 0;font-size:11px;color:#999;'>Build: ").append(buildName).append(" | ").append(timestamp).append("</p>\n");
        html.append("</div>\n");

        html.append("</div>\n"); // container
        html.append("</body></html>");
        return html.toString();
    }

    /**
     * Add a KPI cell to email HTML table.
     */
    private static void addKpiCell(StringBuilder html, String label, String value, String color, String bgColor) {
        html.append("      <td style='text-align:center;padding:14px 8px;background:").append(bgColor)
                .append(";border-radius:8px;'>\n");
        html.append("        <div style='font-size:28px;font-weight:700;color:").append(color).append(";'>")
                .append(value).append("</div>\n");
        html.append("        <div style='font-size:11px;color:#666;text-transform:uppercase;letter-spacing:0.5px;margin-top:4px;'>")
                .append(label).append("</div>\n");
        html.append("      </td>\n");
    }

    /**
     * Add an environment row to email HTML.
     */
    private static void addEnvRow(StringBuilder html, String label, String value) {
        html.append("    <tr style='border-bottom:1px solid #eee;'>\n");
        html.append("      <td style='padding:8px 12px;font-weight:600;color:#546e7a;width:40%;'>").append(label).append("</td>\n");
        html.append("      <td style='padding:8px 12px;color:#333;'>").append(value).append("</td>\n");
        html.append("    </tr>\n");
    }

    // ===== HTML HELPER METHODS =====

    private static void addEnvItem(StringBuilder html, String label, String value, String icon) {
        html.append("    <div class=\"env-item\"><span class=\"env-icon\">").append(icon)
                .append("</span><span class=\"env-label\">").append(label)
                .append("</span><span class=\"env-value\">").append(value).append("</span></div>\n");
    }

    private static void addKpiCard(StringBuilder html, String label, String value, String type, String icon) {
        html.append("    <div class=\"kpi-card kpi-").append(type).append("\">")
                .append("<div class=\"kpi-icon\">").append(icon).append("</div>")
                .append("<div class=\"kpi-number\">").append(value).append("</div>")
                .append("<div class=\"kpi-label\">").append(label).append("</div></div>\n");
    }

    private static void buildDonutChart(StringBuilder html, String title, int total,
                                         int passed, int failed, int skipped, double passRate) {
        html.append("    <div class=\"chart-card\">\n");
        html.append("      <h3>").append(title).append("</h3>\n");
        html.append("      <div class=\"donut-chart\">\n");
        double passAngle = total > 0 ? (passed * 360.0 / total) : 0;
        double failAngle = total > 0 ? (failed * 360.0 / total) : 0;
        html.append("        <div class=\"donut\" style=\"background: conic-gradient(#28a745 0deg ")
                .append(String.format("%.1f", passAngle)).append("deg, #dc3545 ")
                .append(String.format("%.1f", passAngle)).append("deg ")
                .append(String.format("%.1f", passAngle + failAngle)).append("deg, #ffc107 ")
                .append(String.format("%.1f", passAngle + failAngle)).append("deg 360deg);\">\n");
        html.append("          <div class=\"donut-hole\">").append(String.format("%.0f%%", passRate)).append("</div>\n");
        html.append("        </div>\n");
        html.append("      </div>\n");
        html.append("      <div class=\"chart-legend\">\n");
        html.append("        <span class=\"legend-item\"><span class=\"legend-dot\" style=\"background:#28a745\"></span>Passed (").append(passed).append(")</span>\n");
        html.append("        <span class=\"legend-item\"><span class=\"legend-dot\" style=\"background:#dc3545\"></span>Failed (").append(failed).append(")</span>\n");
        html.append("        <span class=\"legend-item\"><span class=\"legend-dot\" style=\"background:#ffc107\"></span>Skipped (").append(skipped).append(")</span>\n");
        html.append("      </div>\n");
        html.append("    </div>\n");
    }

    private static void addProgressBar(StringBuilder html, String label, int passed, int failed, int skipped, int total) {
        double pPct = total > 0 ? (passed * 100.0 / total) : 0;
        double fPct = total > 0 ? (failed * 100.0 / total) : 0;
        double sPct = total > 0 ? (skipped * 100.0 / total) : 0;
        html.append("        <div class=\"progress-item\">\n");
        html.append("          <div class=\"progress-label\">").append(label).append(" <span class=\"progress-count\">").append(passed).append("/").append(total).append("</span></div>\n");
        html.append("          <div class=\"progress-bar\">\n");
        html.append("            <div class=\"progress-fill pass\" style=\"width:").append(String.format("%.1f", pPct)).append("%\"></div>\n");
        html.append("            <div class=\"progress-fill fail\" style=\"width:").append(String.format("%.1f", fPct)).append("%\"></div>\n");
        html.append("            <div class=\"progress-fill skip\" style=\"width:").append(String.format("%.1f", sPct)).append("%\"></div>\n");
        html.append("          </div>\n");
        html.append("        </div>\n");
    }

    private static String formatDuration(long nanos) {
        long millis = nanos / 1_000_000;
        long seconds = millis / 1000;
        long mins = seconds / 60;
        seconds = seconds % 60;
        millis = millis % 1000;
        if (mins > 0) return String.format("%dm %ds %dms", mins, seconds, millis);
        if (seconds > 0) return String.format("%ds %dms", seconds, millis);
        return String.format("%dms", millis);
    }

    /**
     * Build the QMetry Summary HTML content.
     */
    private static String buildQMetryHtml(ReportSummary s) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String overallStatus = s.failedScenarios == 0 ? "PASS" : "FAIL";
        String statusColor = s.failedScenarios == 0 ? "#28a745" : "#dc3545";
        boolean isRemote = "remote".equalsIgnoreCase(executionMode);

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n");
        html.append("<meta charset=\"UTF-8\">\n<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("<title>QMetry Test Execution Report - ").append(PROJECT_NAME).append("</title>\n");
        html.append("<style>\n");
        html.append(getReportCSS());
        html.append("</style>\n</head>\n<body>\n");

        // Header
        html.append("<div class=\"header\">\n");
        html.append("  <div class=\"header-content\">\n");
        html.append("    <div>\n");
        html.append("      <h1>&#x1F4CA; QMetry Test Execution Report</h1>\n");
        html.append("      <p class=\"subtitle\">").append(PROJECT_NAME).append(" | Generated: ").append(timestamp);
        if (isRemote) html.append(" | HyperExecute Cloud");
        html.append("</p>\n");
        html.append("    </div>\n");
        html.append("    <div class=\"overall-status\" style=\"background:").append(statusColor).append("\">\n");
        html.append("      ").append(overallStatus).append("\n");
        html.append("    </div>\n");
        html.append("  </div>\n");
        html.append("</div>\n\n");

        // Env Info
        html.append("<div class=\"container\">\n");
        html.append("<div class=\"env-section\">\n");
        html.append("  <h2>&#x2699;&#xFE0F; Execution Environment</h2>\n");
        html.append("  <div class=\"env-grid\">\n");
        html.append("    <div class=\"env-item\"><span class=\"env-label\">Platform</span><span class=\"env-value\">").append(System.getProperty("os.name")).append("</span></div>\n");
        html.append("    <div class=\"env-item\"><span class=\"env-label\">Java</span><span class=\"env-value\">").append(System.getProperty("java.version")).append("</span></div>\n");
        html.append("    <div class=\"env-item\"><span class=\"env-label\">Browser</span><span class=\"env-value\">").append(System.getProperty("browser", "Chrome")).append("</span></div>\n");
        html.append("    <div class=\"env-item\"><span class=\"env-label\">Mode</span><span class=\"env-value\">").append(isRemote ? "HyperExecute Cloud" : "Local").append("</span></div>\n");
        html.append("    <div class=\"env-item\"><span class=\"env-label\">Build</span><span class=\"env-value\">").append(buildName).append("</span></div>\n");
        html.append("    <div class=\"env-item\"><span class=\"env-label\">Duration</span><span class=\"env-value\">").append(String.format("%.2fs", s.totalDurationSec)).append("</span></div>\n");
        html.append("  </div>\n</div>\n\n");

        // KPI Cards
        html.append("<div class=\"kpi-section\">\n");
        html.append("  <h2>&#x1F4C8; Test Execution Summary</h2>\n");
        html.append("  <div class=\"kpi-grid\">\n");
        html.append("    <div class=\"kpi-card\"><div class=\"kpi-number\">").append(s.totalScenarios).append("</div><div class=\"kpi-label\">Total Scenarios</div></div>\n");
        html.append("    <div class=\"kpi-card kpi-pass\"><div class=\"kpi-number\">").append(s.passedScenarios).append("</div><div class=\"kpi-label\">Passed</div></div>\n");
        html.append("    <div class=\"kpi-card kpi-fail\"><div class=\"kpi-number\">").append(s.failedScenarios).append("</div><div class=\"kpi-label\">Failed</div></div>\n");
        html.append("    <div class=\"kpi-card kpi-skip\"><div class=\"kpi-number\">").append(s.skippedScenarios).append("</div><div class=\"kpi-label\">Skipped</div></div>\n");
        html.append("    <div class=\"kpi-card kpi-rate\"><div class=\"kpi-number\">").append(String.format("%.1f%%", s.passRate)).append("</div><div class=\"kpi-label\">Pass Rate</div></div>\n");
        html.append("  </div>\n</div>\n\n");

        // Charts
        html.append("<div class=\"charts-section\">\n");
        html.append("  <h2>&#x1F4CA; Visual Analytics</h2>\n");
        html.append("  <div class=\"charts-grid\">\n");

        // Scenario donut
        html.append("    <div class=\"chart-card\">\n");
        html.append("      <h3>Scenario Results</h3>\n");
        html.append("      <div class=\"donut-chart\">\n");
        double passAngle = s.totalScenarios > 0 ? (s.passedScenarios * 360.0 / s.totalScenarios) : 0;
        double failAngle = s.totalScenarios > 0 ? (s.failedScenarios * 360.0 / s.totalScenarios) : 0;
        html.append("        <div class=\"donut\" style=\"background: conic-gradient(#28a745 0deg ").append(String.format("%.1f", passAngle))
                .append("deg, #dc3545 ").append(String.format("%.1f", passAngle)).append("deg ")
                .append(String.format("%.1f", passAngle + failAngle)).append("deg, #ffc107 ")
                .append(String.format("%.1f", passAngle + failAngle)).append("deg 360deg);\">\n");
        html.append("          <div class=\"donut-hole\">").append(String.format("%.0f%%", s.passRate)).append("</div>\n");
        html.append("        </div>\n</div>\n");
        html.append("      <div class=\"chart-legend\">\n");
        html.append("        <span class=\"legend-item\"><span class=\"legend-dot\" style=\"background:#28a745\"></span>Passed (").append(s.passedScenarios).append(")</span>\n");
        html.append("        <span class=\"legend-item\"><span class=\"legend-dot\" style=\"background:#dc3545\"></span>Failed (").append(s.failedScenarios).append(")</span>\n");
        html.append("        <span class=\"legend-item\"><span class=\"legend-dot\" style=\"background:#ffc107\"></span>Skipped (").append(s.skippedScenarios).append(")</span>\n");
        html.append("      </div>\n    </div>\n");

        // Steps donut
        html.append("    <div class=\"chart-card\">\n");
        html.append("      <h3>Step Results</h3>\n");
        html.append("      <div class=\"donut-chart\">\n");
        double sPassAngle = s.totalSteps > 0 ? (s.passedSteps * 360.0 / s.totalSteps) : 0;
        double sFailAngle = s.totalSteps > 0 ? (s.failedSteps * 360.0 / s.totalSteps) : 0;
        double stepPassRate = s.totalSteps > 0 ? (s.passedSteps * 100.0 / s.totalSteps) : 0;
        html.append("        <div class=\"donut\" style=\"background: conic-gradient(#28a745 0deg ").append(String.format("%.1f", sPassAngle))
                .append("deg, #dc3545 ").append(String.format("%.1f", sPassAngle)).append("deg ")
                .append(String.format("%.1f", sPassAngle + sFailAngle)).append("deg, #ffc107 ")
                .append(String.format("%.1f", sPassAngle + sFailAngle)).append("deg 360deg);\">\n");
        html.append("          <div class=\"donut-hole\">").append(String.format("%.0f%%", stepPassRate)).append("</div>\n");
        html.append("        </div>\n</div>\n");
        html.append("      <div class=\"chart-legend\">\n");
        html.append("        <span class=\"legend-item\"><span class=\"legend-dot\" style=\"background:#28a745\"></span>Passed (").append(s.passedSteps).append(")</span>\n");
        html.append("        <span class=\"legend-item\"><span class=\"legend-dot\" style=\"background:#dc3545\"></span>Failed (").append(s.failedSteps).append(")</span>\n");
        html.append("        <span class=\"legend-item\"><span class=\"legend-dot\" style=\"background:#ffc107\"></span>Skipped (").append(s.skippedSteps).append(")</span>\n");
        html.append("      </div>\n    </div>\n");

        html.append("  </div>\n</div>\n\n");

        // Scenario Table
        html.append("<div class=\"table-section\">\n");
        html.append("  <h2>&#x1F4DD; Scenario Details</h2>\n");
        html.append("  <table class=\"results-table\">\n");
        html.append("    <thead>\n");
        html.append("      <tr><th>#</th><th>Test Case ID</th><th>Feature</th><th>Scenario</th><th>Steps</th><th>Duration</th><th>Status</th></tr>\n");
        html.append("    </thead>\n<tbody>\n");

        for (int i = 0; i < s.scenarioResults.size(); i++) {
            ScenarioResult sr = s.scenarioResults.get(i);
            String statusClass = switch (sr.status) {
                case "PASS" -> "status-pass";
                case "FAIL" -> "status-fail";
                default -> "status-skip";
            };
            String statusIcon = switch (sr.status) {
                case "PASS" -> "&#x2705;";
                case "FAIL" -> "&#x274C;";
                default -> "&#x23ED;&#xFE0F;";
            };
            html.append("      <tr><td>").append(i + 1).append("</td><td><code>TC-").append(String.format("%03d", i + 1))
                    .append("</code></td><td>").append(sr.featureName).append("</td><td>").append(sr.scenarioName)
                    .append("</td><td>").append(sr.passedSteps).append("/").append(sr.totalSteps)
                    .append("</td><td>").append(formatDuration((long) sr.durationNanos))
                    .append("</td><td><span class=\"status-badge ").append(statusClass).append("\">").append(statusIcon).append(" ").append(sr.status).append("</span></td></tr>\n");

            if (!sr.failureMessage.isEmpty()) {
                html.append("      <tr class=\"failure-row\"><td colspan=\"7\"><div class=\"failure-detail\">")
                        .append(sr.failureMessage).append("</div></td></tr>\n");
            }
        }

        html.append("    </tbody>\n  </table>\n</div>\n\n");

        // Cross-reference to HyperExecute report
        html.append("<div class=\"nav-section\">\n");
        html.append("  <h2>&#x1F517; Related Reports</h2>\n");
        html.append("  <div class=\"related-grid\">\n");
        html.append("    <a class=\"related-card\" href=\"qmetry-hyperexecute.html\">&#x1F680; HyperExecute Integrated Report</a>\n");
        html.append("    <a class=\"related-card\" href=\"extent.html\">&#x1F4D1; Extent Spark Report</a>\n");
        html.append("    <a class=\"related-card\" href=\"cucumber.html\">&#x1F952; Cucumber HTML Report</a>\n");
        html.append("    <a class=\"related-card\" href=\"qmetry-results.json\" target=\"_blank\">&#x1F4BE; JSON Export</a>\n");
        html.append("  </div>\n");
        html.append("</div>\n\n");

        // Footer
        html.append("<div class=\"footer\">\n");
        html.append("  <p>QMetry Test Execution Report | ").append(PROJECT_NAME).append(" | Generated by QMetryReportManager</p>\n");
        html.append("  <p>Report Time: ").append(timestamp).append(" | Build: ").append(buildName).append("</p>\n");
        html.append("</div>\n");

        html.append("</div>\n</body>\n</html>");
        return html.toString();
    }

    /**
     * CSS styles for the QMetry summary report.
     */
    private static String getReportCSS() {
        return """
                * { margin: 0; padding: 0; box-sizing: border-box; }
                body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #f0f2f5; color: #333; }
                .header { background: linear-gradient(135deg, #1a237e 0%, #283593 50%, #3949ab 100%); color: white; padding: 30px 40px; }
                .header-content { display: flex; justify-content: space-between; align-items: center; max-width: 1400px; margin: 0 auto; }
                .header h1 { font-size: 28px; font-weight: 600; }
                .subtitle { font-size: 14px; opacity: 0.85; margin-top: 5px; }
                .overall-status { font-size: 24px; font-weight: 700; padding: 15px 30px; border-radius: 12px; color: white; text-align: center; min-width: 120px; }
                .container { max-width: 1400px; margin: 0 auto; padding: 20px 40px; }
                .env-section, .kpi-section, .charts-section, .table-section, .nav-section { background: white; border-radius: 12px; padding: 25px 30px; margin-bottom: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
                h2 { font-size: 20px; color: #1a237e; margin-bottom: 20px; padding-bottom: 10px; border-bottom: 2px solid #e8eaf6; }
                .env-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 15px; }
                .env-item { background: #f5f5f5; padding: 12px 16px; border-radius: 8px; display: flex; flex-direction: column; }
                .env-label { font-size: 12px; color: #666; text-transform: uppercase; letter-spacing: 0.5px; }
                .env-value { font-size: 15px; font-weight: 600; margin-top: 4px; color: #1a237e; }
                .kpi-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(160px, 1fr)); gap: 15px; }
                .kpi-card { background: #f8f9fa; border-radius: 12px; padding: 20px; text-align: center; border-left: 4px solid #1a237e; }
                .kpi-card.kpi-pass { border-left-color: #28a745; } .kpi-card.kpi-fail { border-left-color: #dc3545; }
                .kpi-card.kpi-skip { border-left-color: #ffc107; } .kpi-card.kpi-rate { border-left-color: #17a2b8; }
                .kpi-number { font-size: 36px; font-weight: 700; color: #1a237e; }
                .kpi-pass .kpi-number { color: #28a745; } .kpi-fail .kpi-number { color: #dc3545; }
                .kpi-skip .kpi-number { color: #ffc107; } .kpi-rate .kpi-number { color: #17a2b8; }
                .kpi-label { font-size: 13px; color: #666; margin-top: 5px; text-transform: uppercase; letter-spacing: 0.5px; }
                .charts-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 20px; }
                .chart-card { text-align: center; padding: 20px; background: #fafafa; border-radius: 10px; }
                .chart-card h3 { color: #333; margin-bottom: 15px; }
                .donut-chart { display: flex; justify-content: center; margin-bottom: 15px; }
                .donut { width: 180px; height: 180px; border-radius: 50%; display: flex; align-items: center; justify-content: center; }
                .donut-hole { width: 110px; height: 110px; border-radius: 50%; background: white; display: flex; align-items: center; justify-content: center; font-size: 28px; font-weight: 700; color: #1a237e; }
                .chart-legend { display: flex; justify-content: center; gap: 20px; flex-wrap: wrap; }
                .legend-item { display: flex; align-items: center; gap: 6px; font-size: 13px; }
                .legend-dot { width: 12px; height: 12px; border-radius: 50%; display: inline-block; }
                .results-table { width: 100%; border-collapse: collapse; font-size: 14px; }
                .results-table th { background: #1a237e; color: white; padding: 12px 16px; text-align: left; font-weight: 600; }
                .results-table td { padding: 12px 16px; border-bottom: 1px solid #eee; }
                .results-table tbody tr:hover { background: #f5f5ff; }
                .results-table code { background: #e8eaf6; padding: 2px 8px; border-radius: 4px; font-size: 13px; color: #1a237e; }
                .status-badge { padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: 600; display: inline-block; }
                .status-pass { background: #d4edda; color: #155724; } .status-fail { background: #f8d7da; color: #721c24; } .status-skip { background: #fff3cd; color: #856404; }
                .failure-row td { background: #fff5f5; }
                .failure-detail { color: #721c24; font-size: 13px; padding: 8px 12px; background: #f8d7da; border-radius: 6px; border-left: 3px solid #dc3545; }
                .related-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 12px; }
                .related-card { display: block; padding: 16px 20px; background: #e8eaf6; border-radius: 10px; text-decoration: none; color: #1a237e; font-weight: 600; text-align: center; transition: all 0.2s; }
                .related-card:hover { background: #c5cae9; transform: translateY(-2px); }
                .footer { text-align: center; padding: 25px; color: #666; font-size: 13px; }
                .footer p { margin: 3px 0; }
                @media (max-width: 768px) { .header-content { flex-direction: column; gap: 15px; } .container { padding: 10px 15px; } .kpi-grid { grid-template-columns: repeat(2, 1fr); } }
                """;
    }

    /**
     * CSS for the HyperExecute integrated report.
     */
    private static String getHyperExecuteReportCSS() {
        return """
                * { margin: 0; padding: 0; box-sizing: border-box; }
                body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #0f1117; color: #e0e0e0; }
                .header { background: linear-gradient(135deg, #0d47a1 0%, #1565c0 40%, #0097a7 100%); color: white; padding: 30px 40px; }
                .header-content { display: flex; justify-content: space-between; align-items: center; max-width: 1400px; margin: 0 auto; }
                .header-left h1 { font-size: 28px; font-weight: 700; }
                .subtitle { font-size: 14px; opacity: 0.9; margin-top: 4px; }
                .timestamp { font-size: 12px; opacity: 0.7; margin-top: 2px; }
                .header-right { display: flex; align-items: center; gap: 15px; }
                .overall-badge { font-size: 22px; font-weight: 700; padding: 12px 28px; border-radius: 12px; color: white; }
                .pass-rate-circle { width: 60px; height: 60px; border-radius: 50%; background: rgba(255,255,255,0.15); display: flex; align-items: center; justify-content: center; }
                .pass-rate-circle span { font-size: 16px; font-weight: 700; }
                .container { max-width: 1400px; margin: 0 auto; padding: 20px 40px; }
                .report-nav, .section { background: #1a1d26; border-radius: 12px; padding: 25px 30px; margin-bottom: 20px; border: 1px solid #2a2d3a; }
                .report-nav h3 { color: #90caf9; margin-bottom: 15px; font-size: 16px; }
                .nav-cards { display: flex; flex-wrap: wrap; gap: 10px; }
                .nav-card { display: flex; align-items: center; gap: 8px; padding: 10px 18px; background: #252833; border-radius: 8px; text-decoration: none; color: #b0bec5; font-size: 13px; font-weight: 500; transition: all 0.2s; border: 1px solid #333644; }
                .nav-card:hover, .nav-card.active { background: #0d47a1; color: white; border-color: #1976d2; }
                .nav-icon { font-size: 18px; }
                h2 { font-size: 20px; color: #90caf9; margin-bottom: 20px; padding-bottom: 10px; border-bottom: 2px solid #2a2d3a; }
                .env-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 12px; }
                .env-item { background: #252833; padding: 14px 16px; border-radius: 10px; display: flex; flex-direction: column; gap: 4px; border: 1px solid #333644; }
                .env-icon { font-size: 20px; }
                .env-label { font-size: 11px; color: #78909c; text-transform: uppercase; letter-spacing: 0.8px; }
                .env-value { font-size: 14px; font-weight: 600; color: #e0e0e0; }
                .kpi-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(150px, 1fr)); gap: 12px; }
                .kpi-card { background: #252833; border-radius: 12px; padding: 18px; text-align: center; border: 1px solid #333644; border-top: 3px solid #546e7a; }
                .kpi-card.kpi-pass { border-top-color: #4caf50; } .kpi-card.kpi-fail { border-top-color: #f44336; }
                .kpi-card.kpi-skip { border-top-color: #ff9800; } .kpi-card.kpi-rate { border-top-color: #00bcd4; }
                .kpi-card.kpi-total { border-top-color: #7c4dff; }
                .kpi-icon { font-size: 24px; margin-bottom: 6px; }
                .kpi-number { font-size: 32px; font-weight: 700; color: #e0e0e0; }
                .kpi-pass .kpi-number { color: #66bb6a; } .kpi-fail .kpi-number { color: #ef5350; }
                .kpi-skip .kpi-number { color: #ffa726; } .kpi-rate .kpi-number { color: #26c6da; }
                .kpi-label { font-size: 12px; color: #78909c; margin-top: 4px; text-transform: uppercase; letter-spacing: 0.5px; }
                .charts-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 15px; }
                .chart-card { text-align: center; padding: 20px; background: #252833; border-radius: 10px; border: 1px solid #333644; }
                .chart-card h3 { color: #b0bec5; margin-bottom: 15px; font-size: 16px; }
                .donut-chart { display: flex; justify-content: center; margin-bottom: 15px; }
                .donut { width: 170px; height: 170px; border-radius: 50%; display: flex; align-items: center; justify-content: center; }
                .donut-hole { width: 100px; height: 100px; border-radius: 50%; background: #252833; display: flex; align-items: center; justify-content: center; font-size: 26px; font-weight: 700; color: #e0e0e0; }
                .chart-legend { display: flex; justify-content: center; gap: 16px; flex-wrap: wrap; }
                .legend-item { display: flex; align-items: center; gap: 6px; font-size: 12px; color: #b0bec5; }
                .legend-dot { width: 10px; height: 10px; border-radius: 50%; display: inline-block; }
                .progress-bars { display: flex; flex-direction: column; gap: 16px; padding: 10px 0; }
                .progress-item { text-align: left; }
                .progress-label { font-size: 13px; color: #b0bec5; margin-bottom: 6px; display: flex; justify-content: space-between; }
                .progress-count { color: #78909c; font-size: 12px; }
                .progress-bar { height: 18px; background: #333644; border-radius: 9px; display: flex; overflow: hidden; }
                .progress-fill { height: 100%; transition: width 0.5s ease; }
                .progress-fill.pass { background: #4caf50; } .progress-fill.fail { background: #f44336; } .progress-fill.skip { background: #ff9800; }
                .execution-meta { display: flex; justify-content: center; gap: 20px; margin-top: 16px; padding-top: 12px; border-top: 1px solid #333644; }
                .meta-item { font-size: 13px; color: #78909c; }
                .table-controls { margin-bottom: 15px; }
                .filter-pills { display: flex; gap: 8px; flex-wrap: wrap; }
                .pill { padding: 6px 16px; border-radius: 20px; border: 1px solid #333644; background: #252833; color: #b0bec5; cursor: pointer; font-size: 13px; transition: all 0.2s; }
                .pill:hover, .pill.active { background: #0d47a1; color: white; border-color: #1976d2; }
                .pill-pass.active { background: #2e7d32; border-color: #4caf50; }
                .pill-fail.active { background: #c62828; border-color: #f44336; }
                .pill-skip.active { background: #e65100; border-color: #ff9800; }
                .results-table { width: 100%; border-collapse: collapse; font-size: 13px; }
                .results-table th { background: #252833; color: #90caf9; padding: 12px 16px; text-align: left; font-weight: 600; border-bottom: 2px solid #0d47a1; }
                .results-table td { padding: 12px 16px; border-bottom: 1px solid #2a2d3a; }
                .results-table tbody tr:hover { background: #1e2230; }
                .results-table code { background: #333644; padding: 2px 8px; border-radius: 4px; font-size: 12px; color: #90caf9; }
                .status-badge { padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: 600; display: inline-block; }
                .status-pass { background: rgba(76,175,80,0.2); color: #66bb6a; }
                .status-fail { background: rgba(244,67,54,0.2); color: #ef5350; }
                .status-skip { background: rgba(255,152,0,0.2); color: #ffa726; }
                .failure-row td { background: rgba(244,67,54,0.05); }
                .failure-detail { color: #ef5350; font-size: 12px; padding: 8px 12px; background: rgba(244,67,54,0.1); border-radius: 6px; border-left: 3px solid #f44336; }
                .tag-list { margin-left: 6px; }
                .tag { display: inline-block; padding: 1px 6px; background: #333644; border-radius: 3px; font-size: 11px; color: #90caf9; margin-left: 3px; }
                .footer { text-align: center; padding: 25px; border-top: 1px solid #2a2d3a; margin-top: 10px; }
                .footer-content p { color: #546e7a; font-size: 12px; margin: 3px 0; }
                .footer-content strong { color: #90caf9; }
                @media (max-width: 768px) { .header-content { flex-direction: column; gap: 15px; } .container { padding: 10px 15px; } .kpi-grid { grid-template-columns: repeat(2, 1fr); } .nav-cards { flex-direction: column; } }
                """;
    }

    /**
     * Inner class to hold report summary data.
     */
    private static class ReportSummary {
        int featureCount, totalScenarios, passedScenarios, failedScenarios, skippedScenarios;
        int totalSteps, passedSteps, failedSteps, skippedSteps;
        long totalDurationNanos;
        double totalDurationSec, passRate;
        List<ScenarioResult> scenarioResults;

        ReportSummary(int featureCount, int totalScenarios, int passedScenarios, int failedScenarios, int skippedScenarios,
                      int totalSteps, int passedSteps, int failedSteps, int skippedSteps,
                      long totalDurationNanos, double totalDurationSec, double passRate,
                      List<ScenarioResult> scenarioResults) {
            this.featureCount = featureCount;
            this.totalScenarios = totalScenarios;
            this.passedScenarios = passedScenarios;
            this.failedScenarios = failedScenarios;
            this.skippedScenarios = skippedScenarios;
            this.totalSteps = totalSteps;
            this.passedSteps = passedSteps;
            this.failedSteps = failedSteps;
            this.skippedSteps = skippedSteps;
            this.totalDurationNanos = totalDurationNanos;
            this.totalDurationSec = totalDurationSec;
            this.passRate = passRate;
            this.scenarioResults = scenarioResults;
        }
    }

    /**
     * Inner class to hold scenario result data.
     */
    private static class ScenarioResult {
        String featureName;
        String scenarioName;
        String status;
        int totalSteps;
        int passedSteps;
        double durationNanos;
        String failureMessage;
        List<String> tags;

        ScenarioResult(String featureName, String scenarioName, String status,
                       int totalSteps, int passedSteps, double durationNanos, String failureMessage,
                       List<String> tags) {
            this.featureName = featureName;
            this.scenarioName = scenarioName;
            this.status = status;
            this.totalSteps = totalSteps;
            this.passedSteps = passedSteps;
            this.durationNanos = durationNanos;
            this.failureMessage = failureMessage;
            this.tags = tags;
        }
    }
}
