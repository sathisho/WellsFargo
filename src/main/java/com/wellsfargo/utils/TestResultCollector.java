package com.wellsfargo.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates JSON test results that feed the custom HTML dashboard.
 * Produces:
 *   - test-results/meta-info.json
 *   - test-results/summary.json
 *   - test-results/scenario-{n}.json
 */
public class TestResultCollector {

    private static final String OUTPUT_DIR = "test-results";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final List<JsonObject> scenarioResults = new ArrayList<>();

    private static int passCount = 0;
    private static int failCount = 0;
    private static int skipCount = 0;
    private static long suiteStartTime;

    /**
     * Call at suite start.
     */
    public static void startSuite() {
        suiteStartTime = System.currentTimeMillis();
        new File(OUTPUT_DIR).mkdirs();
        scenarioResults.clear();
        passCount = 0;
        failCount = 0;
        skipCount = 0;
    }

    /**
     * Record a scenario result.
     */
    public static void recordScenario(String featureName, String scenarioName,
                                       String status, long durationMs, String errorMessage) {
        JsonObject scenario = new JsonObject();
        scenario.addProperty("feature", featureName);
        scenario.addProperty("scenario", scenarioName);
        scenario.addProperty("status", status);
        scenario.addProperty("duration_ms", durationMs);
        scenario.addProperty("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        if (errorMessage != null && !errorMessage.isEmpty()) {
            scenario.addProperty("error", errorMessage);
        }

        switch (status.toUpperCase()) {
            case "PASS", "PASSED" -> passCount++;
            case "FAIL", "FAILED" -> failCount++;
            case "SKIP", "SKIPPED" -> skipCount++;
        }

        scenarioResults.add(scenario);

        // Write individual scenario JSON
        writeJson(OUTPUT_DIR + "/scenario-" + scenarioResults.size() + ".json", scenario);
    }

    /**
     * Call at suite end — generates summary.json and meta-info.json.
     */
    public static void finishSuite() {
        long totalDuration = System.currentTimeMillis() - suiteStartTime;
        int total = passCount + failCount + skipCount;

        // --- summary.json ---
        JsonObject summary = new JsonObject();
        summary.addProperty("total", total);
        summary.addProperty("passed", passCount);
        summary.addProperty("failed", failCount);
        summary.addProperty("skipped", skipCount);
        summary.addProperty("pass_rate", total > 0 ? Math.round((passCount * 100.0) / total) + "%" : "0%");
        summary.addProperty("duration_ms", totalDuration);
        summary.addProperty("duration_readable", formatDuration(totalDuration));

        JsonArray scenarios = new JsonArray();
        scenarioResults.forEach(scenarios::add);
        summary.add("scenarios", scenarios);

        writeJson(OUTPUT_DIR + "/summary.json", summary);

        // --- meta-info.json ---
        JsonObject meta = new JsonObject();
        meta.addProperty("framework", "WellsFargo Enterprise Automation");
        meta.addProperty("version", "1.0");
        meta.addProperty("environment", System.getProperty("env", "SIT"));
        meta.addProperty("browser", System.getProperty("browser", "chrome"));
        meta.addProperty("execution_date", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        meta.addProperty("java_version", System.getProperty("java.version"));
        meta.addProperty("os", System.getProperty("os.name") + " " + System.getProperty("os.version"));
        meta.addProperty("total_scenarios", total);
        meta.addProperty("result", failCount == 0 ? "PASS" : "FAIL");

        writeJson(OUTPUT_DIR + "/meta-info.json", meta);

        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.println("║        TEST EXECUTION SUMMARY            ║");
        System.out.println("╠══════════════════════════════════════════╣");
        System.out.printf("║  Total: %-5d  Pass: %-5d  Fail: %-5d  ║%n", total, passCount, failCount);
        System.out.printf("║  Pass Rate: %-30s ║%n", summary.get("pass_rate").getAsString());
        System.out.printf("║  Duration: %-31s ║%n", formatDuration(totalDuration));
        System.out.println("╚══════════════════════════════════════════╝\n");
    }

    private static void writeJson(String path, JsonObject json) {
        try (FileWriter writer = new FileWriter(path)) {
            gson.toJson(json, writer);
        } catch (IOException e) {
            System.err.println("⚠ Failed to write: " + path + " — " + e.getMessage());
        }
    }

    private static String formatDuration(long ms) {
        long seconds = ms / 1000;
        long minutes = seconds / 60;
        return minutes + "m " + (seconds % 60) + "s";
    }
}
