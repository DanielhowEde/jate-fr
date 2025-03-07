package core-framwork;

import io.cucumber.java.*;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import utils.ScenarioContext;
import core.reporting.ReportingManager;
import core.base.Hooks;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Hooks {
    public static WebDriver driver;
    public static ScenarioContext scenarioContext;
    private static int stepCounter = 1; // Step counter for numbering screenshots

    @Before
    public void setup(Scenario scenario) {
        scenarioContext = new ScenarioContext();
        driver = new org.openqa.selenium.chrome.ChromeDriver();
        driver.manage().window().maximize();
        ReportingManager.createTest(scenario.getName()); // Start Extent Test
        stepCounter = 1; // Reset step counter at the beginning of each scenario
    }

    @AfterStep
    public void takeScreenshotAfterStep(Scenario scenario) {
        if (scenario.isFailed() || true) { // Always capture a screenshot after each step
            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

            // Format scenario name for filename
            String scenarioName = scenario.getName().replaceAll(" ", "_");
            String screenshotPath = "target/screenshots/" + scenarioName + "_Step" + stepCounter + ".png";

            try {
                Files.createDirectories(new File("target/screenshots").toPath());
                Files.copy(srcFile.toPath(), new File(screenshotPath).toPath());

                // Attach screenshot to Extent Report
                ReportingManager.getTest().addScreenCaptureFromPath(screenshotPath);

                // Attach screenshot to Cucumber Report
                byte[] screenshot = Files.readAllBytes(srcFile.toPath());
                scenario.attach(screenshot, "image/png", "Step " + stepCounter + " Screenshot");

            } catch (IOException e) {
                ReportingManager.getTest().info("Failed to capture screenshot: " + e.getMessage());
            }

            stepCounter++; // Increment step counter for the next step
        }
    }

    @After
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
        ReportingManager.flushReport(); // Save Extent Reports
    }
}