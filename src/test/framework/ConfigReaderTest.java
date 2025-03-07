package com.tests;

import com.framework.utils.ConfigReader;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ConfigReaderTest {
    @Test
    public void testGetProperty() {
        String browser = ConfigReader.getProperty("browser");
        assertNotNull(browser, "Browser property should not be null");
    }

    @Test
    public void testGetIntProperty() {
        int timeout = ConfigReader.getIntProperty("timeout", 5);
        assertTrue(timeout > 0, "Timeout should be greater than 0");
    }

    @Test
    public void testGetBooleanProperty() {
        boolean headless = ConfigReader.getBooleanProperty("headless", false);
        assertNotNull(headless, "Headless property should not be null");
    }
}
