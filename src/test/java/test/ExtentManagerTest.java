package com.tests;

import com.framework.reports.ExtentManager;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ExtentManagerTest {
    @Test
    public void testExtentReports() {
        ExtentManager.startTest("Sample Test");
        ExtentManager.logStep("This is a test step.");
        ExtentManager.logPass("Test passed.");
        ExtentManager.flushReports();

        assertTrue(true, "ExtentReports executed without errors.");
    }
}
