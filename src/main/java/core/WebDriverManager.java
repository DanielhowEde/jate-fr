package core;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import utils.config.ConfigReader;

public class WebDriverManager {
	private static volatile WebDriver driver;

	public static WebDriver getDriver() {
		if (driver == null) {
			synchronized (WebDriverManager.class) {
				if (driver == null) {
					ChromeOptions options = new ChromeOptions();
					String debugAddress = ConfigReader.getProperty("chrome.debug.address");
					if (!debugAddress.isBlank()) {
						options.setExperimentalOption("debuggerAddress", debugAddress);
					}
					if (ConfigReader.getBooleanProperty("headless", false)) {
						options.addArguments("--headless=new");
					}
					driver = new ChromeDriver(options);
				}
			}
		}
		return driver;
	}

	public static void quitDriver() {
		if (driver != null) {
			synchronized (WebDriverManager.class) {
				if (driver != null) {
					driver.quit();
					driver = null;
				}
			}
		}
	}
}

/* To start Chrome in remote-debug mode, add to config.properties:
 *   chrome.debug.address=localhost:9222
 *
 * Then launch Chrome:
 * "C:\Program Files\Google\Chrome\Application\chrome.exe"
 *   --remote-debugging-port=9222
 *   --user-data-dir="C:\Users\%USERNAME%\AppData\Local\Google\Chrome\User Data\Profile 1"
 */
