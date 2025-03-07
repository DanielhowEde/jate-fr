package testcases;

import core.BasePage;
import core.WebDriverManager;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class BasePageTest {
    private WebDriver driver;
    private BasePage basePage;

    @BeforeEach
    public void setup() {
        driver = WebDriverManager.getDriver();
        basePage = new BasePage(driver);
        driver.get("http://localhost:3000"); // Replace with a valid test URL
    }

    @Test
    public void testClickElement() {
        basePage.clickElementByLocator("#submitButton");
        assertTrue(driver.getTitle().contains("Success"), "❌ Click action failed!");
    }

    @Test
    public void testEnterText() {
        basePage.enterTextInField("#username", "test");
        String enteredText = driver.findElement(By.cssSelector("#username")).getDomAttribute("value");
        assertEquals("test", enteredText, "❌ Text input failed!");
    }


    @Test
    public void testElementPresence() {
        assertTrue(basePage.isElementVisible("#Login_button"), "❌ Login button not found!");
    }

    @Test
    public void testScreenshotCapture() {
        String screenshotPath = basePage.captureScreenshot("test_screenshot");
        assertNotNull(screenshotPath, "❌ Screenshot was not captured!");
    }

    @AfterEach
    public void tearDown() {
        WebDriverManager.quitDriver();
    }
}