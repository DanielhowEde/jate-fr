package com.tests;

import com.framework.base.WebDriverManager;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class WebDriverManagerTest {
    @Test
    public void testWebDriverInitialization() {
        WebDriver driver = WebDriverManager.getDriver();
        assertNotNull(driver, "WebDriver should be initialized");
    }
}