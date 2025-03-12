package core;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import io.github.bonigarcia.wdm.WebDriverManager;

public class SetMyWebDriver {
    private static WebDriver driver;

    public static WebDriver getDriver() {
        if (driver == null) {
            String browser = System.getProperty("browser", "chrome").toLowerCase();
            switch (browser) {
                case "chrome":
                    WebDriverManager.chromedriver().setup();
                    driver = new ChromeDriver();
                    break;
                case "firefox":
                    WebDriverManager.firefoxdriver().setup();
                    driver = new FirefoxDriver();
                    break;
                case "edge":
	                WebDriverManager.edgedriver().setup();
	                driver = new EdgeDriver();
	                break;
                default:
                    throw new RuntimeException("❌ Invalid browser: " + browser);
            }
            driver.manage().window().maximize();
        }
        return driver;
    }

    public static void quitDriver() {
        if (driver != null) {
            driver.quit();
            driver = null;
            System.out.println("✅ WebDriver Closed");
        }
    }
}