package com.framework.core.runner;

import org.junit.platform.suite.api.SelectDirectories;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Suite
@SuiteDisplayName("Selenium Test Suite")
@SelectDirectories({TestRunner.TEST_DIRECTORIES})  // Dynamically detect test directories
public class TestRunner {
    public static final String TEST_DIRECTORIES = getTestDirectories();

    private static String getTestDirectories() {
        File rootDir = new File(System.getProperty("user.dir") + "/src/test/framework/"); // Updated path
        List<String> testDirs = new ArrayList<>();

        if (rootDir.isDirectory()) {
            for (File dir : rootDir.listFiles()) {
                if (dir.isDirectory() && dir.getName().endsWith("-tests")) {
                    testDirs.add(dir.getAbsolutePath());
                }
            }
        }

        if (testDirs.isEmpty()) {
            throw new RuntimeException("No test repositories found ending with '-tests' in src/test/framework/.");
        }

        return String.join(",", testDirs);
    }
}
