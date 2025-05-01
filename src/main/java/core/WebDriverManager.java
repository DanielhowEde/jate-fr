/*
 * © 2025 Daniel Ede. This code is licensed for personal or educational use only.
 * Unauthorized reproduction or distribution is prohibited.
 */

package core;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import utils.config.ConfigReader;

import java.util.logging.Logger;
import java.util.Map;
import java.util.logging.Level;

public class WebDriverManager {
    private static WebDriver driver;
    private static final Logger logger = Logger.getLogger(WebDriverManager.class.getName());

    /**
     * Returns WebDriver instance using the default browser from config.properties.
     */
    public static WebDriver getDriver() {
        return getDriver(null);
    }

    /**
     * Returns WebDriver instance based on provided browser name or config.properties setting.
     * @param browserOverride - If provided, uses this browser instead of the config setting.
     */
    public static WebDriver getDriver(String browserOverride) {
        if (driver == null) {
            String browser = (browserOverride != null) ? browserOverride.toLowerCase()
                                                       : ConfigReader.getProperty("browser").toLowerCase();
            boolean isHeadless = Boolean.parseBoolean(ConfigReader.getProperty("headless"));

            try {
                switch (browser) {
                    case "chrome":
                        io.github.bonigarcia.wdm.WebDriverManager.chromedriver().setup();
                        ChromeOptions chromeOptions = new ChromeOptions();
                        
                        chromeOptions.setExperimentalOption("prefs", Map.of(
                        	    "credentials_enable_service", false,
                        	    "profile.password_manager_enabled", false,
                        	    "profile.default_content_setting_values.notifications", 2 // 2 = Block notifications
                        	));
                        chromeOptions.addArguments("--disable-save-password-bubble");
                        	
                        if (isHeadless)
                            chromeOptions.addArguments("--headless=new", "--window-size=1920,1080", "--disable-gpu");
                        driver = new ChromeDriver(chromeOptions);
                        break;

                    case "firefox":
                        io.github.bonigarcia.wdm.WebDriverManager.firefoxdriver().setup();
                        FirefoxOptions firefoxOptions = new FirefoxOptions();
                        firefoxOptions.addPreference("dom.webnotifications.enabled", false);
                        firefoxOptions.addPreference("dom.push.enabled", false); // Optional, extra precaution
                        if (isHeadless) firefoxOptions.addArguments("--headless");
                        driver = new FirefoxDriver(firefoxOptions);
                        break;

                    case "edge":
                        io.github.bonigarcia.wdm.WebDriverManager.edgedriver().setup();
                        EdgeOptions edgeOptions = new EdgeOptions();
                        edgeOptions.setExperimentalOption("prefs", Map.of(
                            "profile.default_content_setting_values.notifications", 2
                        ));
                        edgeOptions.addArguments("--disable-notifications");
                        if (isHeadless) edgeOptions.addArguments("--headless");
                        driver = new EdgeDriver(edgeOptions);
                        break;

                    default:
                        String msg = "❌ Invalid browser specified in config: " + browser;
                        logger.severe(msg);
                        throw new RuntimeException(msg);
                }

                driver.manage().window().maximize();
                logger.info("✅ WebDriver Initialized: " + browser + (isHeadless ? " (Headless Mode)" : ""));
            } catch (Exception e) {
                logger.log(Level.SEVERE, "❌ Failed to initialize WebDriver", e);
                throw new RuntimeException("Failed to initialize WebDriver", e);
            }
        }
        return driver;
    }

    /**
     * Closes and quits the WebDriver instance.
     */
    public static void quitDriver() {
        if (driver != null) {
            try {
                driver.quit();
                logger.info("✅ WebDriver Closed.");
            } catch (Exception e) {
                logger.log(Level.WARNING, "⚠️ Failed to quit WebDriver cleanly", e);
            } finally {
                driver = null;
            }
        }
    }
}
