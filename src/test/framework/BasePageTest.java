package com.tests;

import com.framework.base.BasePage;
import com.framework.base.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import static org.junit.jupiter.api.Assertions.*;

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
        basePage.clickElement("#submitButton"); // Adjust selector as needed
        assertTrue(driver.getTitle().contains("Success"), "❌ Click action failed!");
    }

    @Test
    public void testEnterText() {
        basePage.enterText("#username", "testUser");
        String enteredText = driver.findElement(By.cssSelector("#username")).getAttribute("value");
        assertEquals("testUser", enteredText, "❌ Text input failed!");
    }

    @Test
    public void testSelectDropdown() {
        basePage.selectDropdownByText("#countryDropdown", "United States");
        String selectedValue = driver.findElement(By.cssSelector("#countryDropdown")).getAttribute("value");
        assertEquals("United States", selectedValue, "❌ Dropdown selection failed!");
    }

    @Test
    public void testElementPresence() {
        assertTrue(basePage.isElementPresent("#loginForm"), "❌ Login form not found!");
    }

    @Test
    public void testHandleJavaScriptAlert() {
        basePage.clickElement("#alertButton"); // Trigger alert
        basePage.handleJavaScriptModal("accept", null);
        // No exception = success
        assertTrue(true, "✅ JavaScript alert handled successfully.");
    }

    @Test
    public void testHandleJavaScriptPrompt() {
        basePage.clickElement("#promptButton"); // Trigger prompt
        basePage.handleJavaScriptModal("input", "Test Input");
        assertTrue(true, "✅ JavaScript prompt handled successfully.");
    }

    @Test
    public void testSwitchToAndCloseModal() {
        basePage.clickElement("#openModal"); // Open modal
        basePage.switchToModal("#modalWindow");
        basePage.closeModal("#closeModalButton");
        assertFalse(basePage.isElementPresent("#modalWindow"), "❌ Modal was not closed!");
    }

    @Test
    public void testGetToastMessage() {
        basePage.clickElement("#triggerToast"); // Trigger toast message
        String toastMessage = basePage.getToastMessage(".toast-message");
        assertEquals("Action Successful!", toastMessage, "❌ Toast message incorrect!");
    }

    @Test
    public void testGetElementText() {
        String text = basePage.getElementText("#welcomeMessage");
        assertEquals("Welcome!", text, "❌ Text retrieval failed!");
    }

    @Test
    public void testScreenshotCapture() {
        String screenshotPath = basePage.takeScreenshot("test_screenshot");
        assertNotNull(screenshotPath, "❌ Screenshot was not captured!");
    }

    @AfterEach
    public void tearDown() {
        WebDriverManager.quitDriver();
    }
}
