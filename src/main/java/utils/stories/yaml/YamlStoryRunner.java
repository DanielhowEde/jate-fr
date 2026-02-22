package utils.stories.yaml;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import runner.StoryRunner;
import utils.context.StoryContext;
import utils.excel.ExcelFileManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static core.TestLogger.LOGGER;

/**
 * Orchestrates a YAML-defined story: loads the Excel workbook once, iterates over
 * each {@code run} item in order, injects data into {@link StoryContext}, invokes
 * Cucumber via {@link StoryRunner}, and produces a story-level HTML report.
 *
 * <p>Usage:
 * <pre>
 *   StoryRunResult result = YamlStoryRunner.run("stories/my_story.yaml");
 * </pre>
 */
public class YamlStoryRunner {

    private static final String REPORT_DIR = "../test-reports/";
    private static final String WORKING_COPY_FOLDER = "target/story-excel/";

    /**
     * Executes the story defined in {@code yamlPath} and returns a full result summary.
     *
     * @param yamlPath classpath resource path or file-system path to the story YAML
     * @return {@link StoryRunResult} containing pass/fail details for every item
     */
    public static StoryRunResult run(String yamlPath) {
        // 1. Parse and validate the story YAML
        YamlStoryDefinition story = YamlStoryLoader.load(yamlPath);
        LOGGER.info("Starting story: '{}' (excel: {})", story.getStory(), story.getExcel());

        // 2. Enable story mode so ContextManager routes reads through StoryContext
        StoryContext.enableStoryMode();

        // 3. Create a timestamped working copy of the Excel file (avoids modifying the original)
        String workingCopyPath = ExcelFileManager.createWorkingCopy(story.getExcel(), WORKING_COPY_FOLDER);
        LOGGER.info("Using Excel working copy: {}", workingCopyPath);

        StoryRunResult runResult = new StoryRunResult(story.getStory(), story.getExcel());
        boolean failFast = story.getSettings() != null && story.getSettings().isFailFast();

        // 4. Open the workbook once and reuse it across all items
        try (StoryExcelManager excel = new StoryExcelManager(workingCopyPath)) {

            // 5. Initialize the story-level Extent HTML report
            ExtentReports storyExtent = initStoryReport(story.getStory());
            ExtentTest storyNode = storyExtent.createTest("Story: " + story.getStory());
            if (story.getDescription() != null && !story.getDescription().isBlank()) {
                storyNode.info("Description: " + story.getDescription());
            }
            storyNode.info("Excel: " + story.getExcel());
            storyNode.info("Items: " + story.getRun().size() + " | Fail-fast: " + failFast);

            // 6. Execute each run item in order
            List<YamlRunItem> items = story.getRun();
            for (int i = 0; i < items.size(); i++) {
                YamlRunItem item = items.get(i);
                ItemResult result = new ItemResult(i + 1, item);

                String nodeLabel = "[" + (i + 1) + "] " + item.getFeature()
                    + (item.getScenario() != null ? " — " + item.getScenario() : "");
                ExtentTest itemNode = storyNode.createNode(nodeLabel);

                itemNode.info("Feature: " + item.getFeature());
                if (item.getScenario() != null) {
                    itemNode.info("Scenario filter: " + item.getScenario());
                }

                // 6a. Inject Excel data into StoryContext
                if (item.getData() != null) {
                    itemNode.info("Data: " + item.getData().selectorDescription());
                    try {
                        injectData(item.getData(), excel);
                    } catch (RuntimeException e) {
                        result.setPassed(false);
                        result.setError("Data injection failed: " + e.getMessage());
                        itemNode.fail("Data injection failed: " + e.getMessage());
                        runResult.addItem(result);
                        LOGGER.error("Item [{}] data injection failed: {}", i + 1, e.getMessage());
                        if (failFast) break;
                        continue;
                    }
                }

                // 6b. Run the feature (optionally filtered to a single scenario)
                long start = System.currentTimeMillis();
                byte exitCode;
                try {
                    exitCode = StoryRunner.runFeatureFile(item.getFeature(), item.getScenario());
                } catch (Exception e) {
                    exitCode = 1;
                    result.setError("Runner exception: " + e.getMessage());
                    LOGGER.error("Item [{}] runner threw exception", i + 1, e);
                }
                result.setDurationMs(System.currentTimeMillis() - start);

                // 6c. Record pass/fail
                boolean passed = (exitCode == 0);
                result.setPassed(passed);

                // 6d. Log outcome to Extent report node
                if (passed) {
                    itemNode.pass("PASSED (" + result.getDurationMs() + "ms)");
                } else {
                    String errMsg = result.getError() != null ? result.getError() : "Cucumber exit code: " + exitCode;
                    itemNode.fail("FAILED (" + result.getDurationMs() + "ms) — " + errMsg);
                }

                runResult.addItem(result);
                LOGGER.info("{}", result);

                // 6e. Fail-fast: stop processing remaining items on first failure
                if (failFast && !passed) {
                    LOGGER.warn("Fail-fast enabled — stopping story after item [{}] failure.", i + 1);
                    break;
                }

                // Clear per-item data from context before the next item
                StoryContext.clear();
                StoryContext.enableStoryMode();
            }

            // 7. Summary node
            storyNode.info(runResult.toString());
            if (runResult.isAllPassed()) {
                storyNode.pass("All items passed.");
            } else {
                storyNode.fail(runResult.getFailCount() + " item(s) failed.");
            }

            // 8. Write the story HTML report
            storyExtent.flush();
            LOGGER.info("Story report written to: {}", REPORT_DIR);

        } catch (Exception e) {
            throw new RuntimeException("Story execution failed: " + e.getMessage(), e);
        } finally {
            // 8. Always clean up StoryContext at the end
            StoryContext.clear();
        }

        LOGGER.info("{}", runResult);
        return runResult;
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    /**
     * Resolves the data mapping and injects the result into {@link StoryContext}.
     *
     * <ul>
     *   <li>{@code row: N}     → each column injected as {@code StoryContext.put(column, value)}</li>
     *   <li>{@code key: …}     → same, but row located by column=value lookup</li>
     *   <li>{@code rows: all}  → list injected as {@code StoryContext.put("rows", List<Map>)}</li>
     *   <li>No selector       → first data row (index 1) used</li>
     * </ul>
     */
    private static void injectData(YamlDataMapping data, StoryExcelManager excel) {
        String sheet = data.getSheet();

        if ("all".equalsIgnoreCase(data.getRows())) {
            List<Map<String, String>> rows = excel.getSheetRows(sheet);
            StoryContext.put("rows", rows);
            LOGGER.debug("Injected {} rows from sheet '{}' as 'rows'", rows.size(), sheet);
            return;
        }

        Map<String, String> row;
        if (data.getKey() != null) {
            row = excel.getRowByKey(sheet, data.getKey().getColumn(), data.getKey().getValue());
        } else {
            int rowIndex = (data.getRow() != null) ? data.getRow() : 1;
            row = excel.getRowByIndex(sheet, rowIndex);
        }

        for (Map.Entry<String, String> entry : row.entrySet()) {
            StoryContext.put(entry.getKey(), entry.getValue());
        }
        LOGGER.debug("Injected {} columns from sheet '{}' into StoryContext", row.size(), sheet);
    }

    /**
     * Creates a fresh {@link ExtentReports} writing to a timestamped story HTML file.
     */
    private static ExtentReports initStoryReport(String storyName) {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String safeName  = storyName.replaceAll("[^a-zA-Z0-9_\\-]", "_");
        String reportPath = REPORT_DIR + "story_" + safeName + "_" + timestamp + ".html";

        ExtentSparkReporter spark = new ExtentSparkReporter(reportPath);
        spark.config().setDocumentTitle("Story Report — " + storyName);
        spark.config().setReportName(storyName);
        spark.config().setTheme(Theme.STANDARD);

        ExtentReports extent = new ExtentReports();
        extent.attachReporter(spark);
        extent.setSystemInfo("Story", storyName);
        extent.setSystemInfo("Framework", "YAML Story Runner");
        return extent;
    }
}
