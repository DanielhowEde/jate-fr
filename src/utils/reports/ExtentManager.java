package com.framework.reports;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ExtentManager {
    private static ExtentReports extent;
    private static ExtentTest test;

    public static ExtentReports getInstance() {
        if (extent == null) {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String reportFilePath = "reports/ExtentReport_" + timestamp + ".html";

            ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportFilePath);
            sparkReporter.config().setDocumentTitle("Test Automation Report");
            sparkReporter.config().setReportName("Selenium Automation Testing");
            sparkReporter.config().setTheme(Theme.STANDARD);

            extent = new ExtentReports();
            extent.attachReporter(sparkReporter);
            extent.setSystemInfo("Framework", "Selenium-JUnit");
            extent.setSystemInfo("Tester", System.getProperty("user.name"));
        }
        return extent;
    }

    public static void startTest(String testName) {
        test = getInstance().createTest(testName);
    }

    public static void logStep(String stepDescription) {
        if (test != null) {
            test.info(stepDescription);
        }
    }

    public static void logPass(String message) {
        if (test != null) {
            test.pass(message);
        }
    }

    public static void logFail(String message) {
        if (test != null) {
            test.fail(message);
        }
    }

    public static void attachScreenshot(String screenshotPath) {
        if (test != null) {
            test.addScreenCaptureFromPath(screenshotPath);
        }
    }

    public static void flushReports() {
        if (extent != null) {
            extent.flush();
        }
    }
}
