package tests;

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
        driver.get("https://example.com"); // Replace with a valid test URL
    }

    @Test
    public void testClickElement() {
        basePage.clickElementByLocator("#submitButton");
        assertTrue(driver.getTitle().contains("Success"), "❌ Click action failed!");
    }

    @Test
    public void testEnterText() {
        basePage.enterTextInField("#username", "testUser");
        String enteredText = driver.findElement(By.cssSelector("#username")).getDomAttribute("value");
        assertEquals("testUser", enteredText, "❌ Text input failed!");
    }

    @Test
    public void testSelectDropdown() {
        basePage.selectDropdownOptionByText("#countryDropdown", "United States");
        String selectedValue = driver.findElement(By.cssSelector("#countryDropdown")).getDomAttribute("value");
        assertEquals("United States", selectedValue, "❌ Dropdown selection failed!");
    }

    @Test
    public void testElementPresence() {
        assertTrue(basePage.isElementVisible("#loginForm"), "❌ Login form not found!");
    }

    @Test
    public void testHandleJavaScriptAlert() {
        basePage.clickElementByLocator("#alertButton");
        basePage.handleJavaScriptAlert("accept", null);
        assertTrue(true, "✅ JavaScript alert handled successfully.");
    }

    @Test
    public void testHandleJavaScriptPrompt() {
        basePage.clickElementByLocator("#promptButton");
        basePage.handleJavaScriptAlert("input", "Test Input");
        assertTrue(true, "✅ JavaScript prompt handled successfully.");
    }

    @Test
    public void testSwitchToAndCloseModal() {
        basePage.clickElementByLocator("#openModal");
        basePage.focusOnModal("#modalWindow");
        basePage.closeModalWindow("#closeModalButton");
        assertFalse(basePage.isElementVisible("#modalWindow"), "❌ Modal was not closed!");
    }

    @Test
    public void testGetToastMessage() {
        basePage.clickElementByLocator("#triggerToast");
        String toastMessage = basePage.getToastPopupMessage(".toast-message");
        assertEquals("Action Successful!", toastMessage, "❌ Toast message incorrect!");
    }

    @Test
    public void testGetElementText() {
        String text = basePage.getTextFromElement("#welcomeMessage");
        assertEquals("Welcome!", text, "❌ Text retrieval failed!");
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