package core;

import io.cucumber.java.*;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Hooks {
    private static WebDriver driver;
    private static int stepCounter = 1; // Step counter for naming screenshots

    @Before
    public void setup(Scenario scenario) {
        driver = SetMyWebDriver.getDriver(); // Initialize WebDriver using SetMyWebDriver
        stepCounter = 1; // Reset step counter at the beginning of each scenario
    }

    @AfterStep
    public void takeScreenshotAfterStep(Scenario scenario) {
        File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

        // Format scenario name for filename
        String scenarioName = scenario.getName().replaceAll(" ", "_");
        String screenshotPath = "target/screenshots/" + scenarioName + "_Step" + stepCounter + ".png";

        try {
            Files.createDirectories(new File("target/screenshots").toPath());
            Files.copy(srcFile.toPath(), new File(screenshotPath).toPath());
            System.out.println("📸 Screenshot saved: " + screenshotPath);

            // Attach screenshot to Cucumber Report
            byte[] screenshot = Files.readAllBytes(srcFile.toPath());
            scenario.attach(screenshot, "image/png", "Step " + stepCounter + " Screenshot");

        } catch (IOException e) {
            System.out.println("❌ Failed to save screenshot: " + e.getMessage());
        }

        stepCounter++; // Increment step counter for the next step
    }

    @After
    public void tearDown() {
        SetMyWebDriver.quitDriver(); // Quit WebDriver properly
    }
}