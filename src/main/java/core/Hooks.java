package core;

import io.cucumber.java.*;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import utils.context.ScenarioContext;
import io.cucumber.java.Status;
import listeners.StepLogger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Hooks {
    private static final Logger logger = Logger.getLogger(Hooks.class.getName());
    private static WebDriver driver;
    private static int stepCounter = 1;
    private static ExtentReports extentReports;
    private static ExtentTest extentTest;
    private static ScenarioContext scenarioContext = new ScenarioContext();
    private static String reportDir;

    public static ExtentTest getExtentTest() {
        return extentTest;
    }

    @BeforeAll
    public static void setupReport() throws IOException {
        try {
            LogManager.getLogManager().readConfiguration(
                Hooks.class.getClassLoader().getResourceAsStream("logging.properties")
            );
        } catch (IOException e) {
            System.err.println("❌ Could not load logging config: " + e.getMessage());
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        reportDir = timestamp + "_test_reports";
        Files.createDirectories(Path.of(reportDir)); // create the main directory
        String reportPath = reportDir + "/ExtentReport.html";

        ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);
        
        sparkReporter.config().setDocumentTitle("Automation Test Report");
        sparkReporter.config().setReportName("Cucumber Selenium Test Execution");

        extentReports = new ExtentReports();
        extentReports.attachReporter(sparkReporter);
        logger.info("📊 Extent Reports initialized.");
    }

    @Before
    public void setup(Scenario scenario) {
        driver = core.WebDriverManager.getDriver();
        driver.manage().window().maximize();
        extentTest = extentReports.createTest(scenario.getName());

        if (!scenarioContext.contains("spreadsheet")) {
            extractSpreadsheetName(scenario);
        }

        logger.info("✅ WebDriver Initialized");
    }

    @AfterStep
    public void takeScreenshotAfterStep(Scenario scenario) throws IOException {
        File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        String scenarioName = scenario.getName().replaceAll(" ", "_");
        String screenshotDir = reportDir + "/screenshots";
        Files.createDirectories(Path.of(screenshotDir));
        String stepText = StepLogger.getCurrentStepText()
        	    .or(() -> Optional.ofNullable((String) scenarioContext.get("lastStepText")))
        	    .orElse("Step " + stepCounter);
        System.out.println("👣 Step used in report: " + stepText);
        
        String cleanStep = stepText.replaceAll("[^a-zA-Z0-9-_]", "_");
        String screenshotPath = screenshotDir + "/" + scenarioName + "_" + cleanStep + ".png";
        
        try {
            Files.createDirectories(Path.of("target/screenshots"));
            Files.copy(srcFile.toPath(), Path.of(screenshotPath));
            logger.info("Screenshot saved: " + screenshotPath);

            byte[] screenshot = Files.readAllBytes(srcFile.toPath());
            scenario.attach(screenshot, "image/png", stepText + " Screenshot");

            extentTest.addScreenCaptureFromPath(screenshotPath);

            Status status = scenario.getStatus();
            if (scenario.isFailed()) {
                extentTest.fail("❌ '" + stepText + "' failed.").addScreenCaptureFromPath(screenshotPath);
            } else if (status == Status.SKIPPED || status == Status.UNDEFINED) {
                extentTest.skip("⚠️ '" + stepText + "' was skipped or undefined.").addScreenCaptureFromPath(screenshotPath);
            } else {
                extentTest.pass("✅ '" + stepText + "' passed.").addScreenCaptureFromPath(screenshotPath);
            }

        } catch (IOException e) {
            logger.log(Level.SEVERE, "❌ Failed to save screenshot: " + e.getMessage());
        }

        stepCounter++;
    }

    @After
    public void tearDown(Scenario scenario) throws IOException {
    	Status finalStatus = scenario.getStatus();

        if (finalStatus == Status.PASSED) {
            extentTest.pass("Scenario passed successfully!");
        } else {
            String message;
            if (scenario.isFailed()) {
                message = "Scenario failed due to an error.";
            } else if (finalStatus == Status.UNDEFINED || finalStatus == Status.SKIPPED) {
                message = "Scenario failed due to skipped/undefined steps.";
            } else {
                message = "Scenario ended with status: " + finalStatus;
            }

            String screenshotPath = captureScreenshot(scenario);
            if (screenshotPath != null) {
                extentTest.fail(message).addScreenCaptureFromPath(screenshotPath);
            } else {
                extentTest.fail(message);
            }
        }

        try {
            core.WebDriverManager.quitDriver();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Failed to quit browser: " + e.getMessage(), e);
        }
    }

    @AfterAll
    public static void flushReports() {
        extentReports.flush();
        logger.info("Extent Reports Generated: target/ExtentReport.html");

        scenarioContext = new ScenarioContext();
        logger.info("ScenarioContext cleared after feature execution.");

        try {
            core.WebDriverManager.quitDriver();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Failed to quit browser: " + e.getMessage(), e);
        }
        tidyDir();
    }

    private String captureScreenshot(Scenario scenario) throws IOException {
        File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        String scenarioName = scenario.getName().replaceAll(" ", "_");
        String screenshotPath = reportDir + "/screenshots/" + scenarioName + "_Failed.png";
        Files.createDirectories(Path.of(reportDir + "/screenshots"));

        try {
            Files.createDirectories(Path.of("target/screenshots"));
            Files.copy(srcFile.toPath(), Path.of(screenshotPath));
            return screenshotPath;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "❌ Failed to capture failure screenshot: " + e.getMessage());
            return null;
        }
    }

    private void extractSpreadsheetName(Scenario scenario) {
        Optional<String> spreadsheetTag = scenario.getSourceTagNames().stream()
            .filter(tag -> tag.startsWith("@Data_"))
            .findFirst();

        if (spreadsheetTag.isPresent()) {
            String spreadsheetName = spreadsheetTag.get().replace("@Data_", "");
            scenarioContext.set("spreadsheet", spreadsheetName);
            logger.info("Spreadsheet stored in context at FEATURE level: " + spreadsheetName);
        } else {
            logger.warning("⚠️ No @Data_ tag found. Skipping spreadsheet storage.");
        }
    }

    public static ScenarioContext getScenarioContext() {
        return scenarioContext;
    }
    
    private static void tidyDir() {
    	try {
    	    File currentDir = new File("."); // base dir
    	    File[] reportDirs = currentDir.listFiles((dir, name) ->
    	        name.matches("\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}_test_reports")
    	    );

    	    if (reportDirs != null) {
    	        for (File dir : reportDirs) {
    	            if (!dir.getName().equals(reportDir)) {
    	                long lastModified = dir.lastModified();
    	                long ageInMillis = System.currentTimeMillis() - lastModified;
    	                long twoDaysInMillis = 2L * 24 * 60 * 60 * 1000;

    	                if (ageInMillis > twoDaysInMillis) {
    	                    deleteFolderRecursively(dir.toPath());
    	                    logger.info("🧹 Deleted old report folder: " + dir.getName());
    	                }
    	            }
    	        }
    	    }
    	} catch (Exception e) {
    	    logger.log(Level.WARNING, "⚠️ Failed to clean old report directories: " + e.getMessage(), e);
    	}
    }
    
    private static void deleteFolderRecursively(Path path) throws IOException {
        if (Files.exists(path)) {
            Files.walk(path)
                .sorted((a, b) -> b.compareTo(a)) // delete children first
                .forEach(p -> {
                    try {
                        Files.delete(p);
                    } catch (IOException e) {
                        logger.warning("⚠️ Couldn't delete " + p);
                    }
                });
        }
    }
    
}
