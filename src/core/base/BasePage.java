package com.framework.base;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

public class BasePage {
    protected WebDriver driver;
    private WebDriverWait wait;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10)); // Timeout from config
    }

    // Generic method to find elements dynamically (CSS or XPath)
    private By getLocator(String locator) {
        return locator.startsWith("//") ? By.xpath(locator) : By.cssSelector(locator);
    }

    // Wait for an element to be visible & enabled before interacting
    private WebElement waitForElement(String locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(getLocator(locator)));
    }

    // Click an element (button, link, etc.)
    public void clickElement(String locator) {
        try {
            waitForElement(locator).click();
            System.out.println("✅ Clicked element: " + locator);
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to click element: " + locator, e);
        }
    }

    // Enter text into a text field
    public void enterText(String locator, String text) {
        try {
            WebElement element = waitForElement(locator);
            element.clear();
            element.sendKeys(text);
            System.out.println("✅ Entered text: " + text + " in " + locator);
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to enter text in: " + locator, e);
        }
    }

    // Select an item from a dropdown by visible text
    public void selectDropdownByText(String locator, String value) {
        try {
            Select dropdown = new Select(waitForElement(locator));
            dropdown.selectByVisibleText(value);
            System.out.println("✅ Selected dropdown value: " + value);
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to select dropdown value: " + value, e);
        }
    }

    // Check if an element is present on the page
    public boolean isElementPresent(String locator) {
        try {
            driver.findElement(getLocator(locator));
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    // ✅ NEW: Set Checkbox State (Check or Uncheck)
    public void setCheckboxState(String locator, boolean shouldBeChecked) {
        try {
            WebElement checkbox = waitForElement(locator);
            boolean isChecked = checkbox.isSelected();

            if (isChecked != shouldBeChecked) { // Click only if the state needs to be changed
                checkbox.click();
                System.out.println("✅ Checkbox state updated: " + (shouldBeChecked ? "Checked" : "Unchecked"));
            } else {
                System.out.println("ℹ️ Checkbox already in desired state: " + (shouldBeChecked ? "Checked" : "Unchecked"));
            }
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to set checkbox state: " + locator, e);
        }
    }

    // Handle modals (pop-ups)
    public void switchToModal(String modalLocator) {
        try {
            WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(getLocator(modalLocator)));
            driver.switchTo().activeElement();
            System.out.println("✅ Switched to modal.");
        } catch (Exception e) {
            throw new RuntimeException("❌ Modal not found: " + modalLocator, e);
        }
    }

    // Close modal
    public void closeModal(String closeButtonLocator) {
        try {
            clickElement(closeButtonLocator);
            System.out.println("✅ Modal closed.");
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to close modal.", e);
        }
    }

    // Handle JavaScript alerts (accept, dismiss, input)
    public void handleJavaScriptModal(String action, String inputText) {
        try {
            wait.until(ExpectedConditions.alertIsPresent());
            Alert alert = driver.switchTo().alert();

            switch (action.toLowerCase()) {
                case "accept":
                    alert.accept();
                    System.out.println("✅ Accepted JavaScript alert.");
                    break;
                case "dismiss":
                    alert.dismiss();
                    System.out.println("✅ Dismissed JavaScript alert.");
                    break;
                case "input":
                    alert.sendKeys(inputText);
                    alert.accept();
                    System.out.println("✅ Entered text in JavaScript prompt.");
                    break;
                default:
                    throw new RuntimeException("❌ Invalid JavaScript modal action: " + action);
            }
        } catch (Exception e) {
            throw new RuntimeException("❌ No JavaScript alert found.", e);
        }
    }

    // Handle toast popups (wait & get text)
    public String getToastMessage(String toastLocator) {
        try {
            WebElement toast = wait.until(ExpectedConditions.visibilityOfElementLocated(getLocator(toastLocator)));
            return toast.getText();
        } catch (Exception e) {
            throw new RuntimeException("❌ Toast message not found: " + toastLocator, e);
        }
    }

    // Get text from an element
    public String getElementText(String locator) {
        try {
            return waitForElement(locator).getText();
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to get text from element: " + locator, e);
        }
    }

    // Take a screenshot (returns file path)
    public String takeScreenshot(String screenshotName) {
        try {
            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String screenshotPath = "screenshots/" + screenshotName + ".png";
            File destFile = new File(screenshotPath);
            srcFile.renameTo(destFile);
            System.out.println("✅ Screenshot taken: " + screenshotPath);
            return screenshotPath;
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to take screenshot: " + screenshotName, e);
        }
    }
}
