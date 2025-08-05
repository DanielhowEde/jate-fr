package core;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class WebDriverManager {
	private static WebDriver driver;
	
	public static WebDriver getDriver() {
		if (driver == null){
			ChromeOptions options = new ChromeOptions();
			
			options.setExperimentalOption("debuggerAddress", "localhost:9222");
			
			driver = new ChromeDriver(options);
			
		}
		return driver;
	}
	
	public static void quitDriver() {
		if (driver != null) {
			driver.quit();
			driver = null;
			
		}
	}
}

/* To start 
"C:\Program Files\Google\Chrome\Application\chrome.exe" ^
--remote-debugging-port=9222 ^
--user-data-dir="C:\Users\%USERNAME%\AppData\Local\Google\Chrome\User Data\Profile 1"

*/