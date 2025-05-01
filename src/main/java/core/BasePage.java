/*
 * © 2025 Daniel Ede. This code is licensed for personal or educational use only.
 * Unauthorized reproduction or distribution is prohibited.
 */

package core;

import org.jcodec.common.logging.Logger;
import org.openqa.selenium.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import utils.config.ConfigReader;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static core.TestLogger.LOGGER;

import java.io.File;
import java.time.Duration;
import java.util.List;

public class BasePage {
    protected WebDriver driver;
    protected int timeoutInSeconds;
    private WebDriverWait wait;
    private static final long POLLING_INTERVAL_MS = 500;
    

    
    public BasePage(WebDriver driver) {
        this.driver = driver;
        String timeoutStr = ConfigReader.getProperty("webdriver.wait.timeout");
        int timeout;

        try {
            timeout = Integer.parseInt(timeoutStr);
            LOGGER.info("📌 Timeout set to: " + timeout + " seconds.");
        } catch (Exception e) {
            timeout = 40; // fallback default
            LOGGER.info("⚠️ Timeout config missing or invalid. Using default: " + timeout + " seconds.");
        }

        this.timeoutInSeconds = timeout;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
    }

    private By getLocator(String locator) {
        String trimmed = locator.trim();
        if (trimmed.startsWith("/") || trimmed.startsWith("(")) {
            return By.xpath(locator);
        }
        return By.cssSelector(locator);
    }

    protected WebElement waitForElement(String locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(getLocator(locator)));
    }

    private boolean isElementOnTop(WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        String script = """
            var elem = arguments[0];
            var rect = elem.getBoundingClientRect();
            var elFromPoint = document.elementFromPoint(rect.left + rect.width/2, rect.top + rect.height/2);
            return elem === elFromPoint || elem.contains(elFromPoint);
        """;
        return (Boolean) js.executeScript(script, element);
    }

    public void selectDateFromCalendar(String dateFieldLocator, String date) {
        try {
            WebElement dateField = waitForElement(dateFieldLocator);
            dateField.clear();
            dateField.sendKeys(date);
            dateField.sendKeys(Keys.TAB); // Close the calendar if needed
            Logger.info("✅ Date entered: " + date);
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to set date in calendar: " + dateFieldLocator, e);
        }
    }
    
    public void clickElementByLocator(By locator, long timeoutMs) {
        long endTime = System.currentTimeMillis() + timeoutMs;
        boolean clicked = false;
        LOGGER.debug("Attempting to click element(s) located by: " + locator);

        while (System.currentTimeMillis() < endTime && !clicked) {
            // Find all elements matching the locator
            List<WebElement> elements = driver.findElements(locator);
            if (elements.isEmpty()) {
            	 LOGGER.debug("No elements found for locator " + locator + " yet.");
            } else {
            	 LOGGER.info("Found " + elements.size() + " element(s) for locator " + locator + ". Checking clickability...");
            }

            // Iterate through each found element to find a clickable one
            for (WebElement element : elements) {
                if (!element.isDisplayed()) {
                	 LOGGER.debug("Element found but not visible (isDisplayed() == false). Skipping this element.");
                    continue;
                }
                if (!element.isEnabled()) {
                	 LOGGER.debug("Element found but not enabled (isEnabled() == false). Skipping this element.");
                    continue;
                }
                // Check if element is not overlapped by another element at its center point
                JavascriptExecutor js = (JavascriptExecutor) driver;
                Boolean isTopElement = (Boolean) js.executeScript(
                    "var elem = arguments[0];" +
                    "var rect = elem.getBoundingClientRect();" +
                    "var cx = rect.left + (rect.width / 2);" +
                    "var cy = rect.top + (rect.height / 2);" +
                    "var topElem = document.elementFromPoint(cx, cy);" +
                    "return (topElem === elem || elem.contains(topElem));",
                    element
                );

                if (isTopElement != null && isTopElement) {
                    // Element is visible, enabled, and not overlapped – safe to click
                	 LOGGER.info("Clicking element: " + locator);
                    element.click();
                    LOGGER.info("Element clicked successfully.");
                    clicked = true;
                    break;
                } else {
                	 LOGGER.debug("Element is overlapped by another element (not top at its center). Skipping this element.");
                    // continue to next element
                }
            }

            if (!clicked) {
                // No element was clickable in this iteration, wait briefly and retry
                try {
                    Thread.sleep(POLLING_INTERVAL_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while waiting for element to become clickable", ie);
                }
                LOGGER.info("Retrying to find a clickable element for locator: " + locator);
            }
        }
        
        

        if (!clicked) {
            // Timeout reached without clicking any element
            String errorMsg = "Failed to click any element for locator " + locator 
                              + " after " + timeoutMs + " ms (no clickable element found).";
            System.out.println(errorMsg);
            throw new TimeoutException(errorMsg);
        }
    }

    public void clickElementByLocator(String thisLocator) {
    	By locator = this.getLocator(thisLocator);
        long endTime = System.currentTimeMillis() + timeoutInSeconds * 1000L;
        boolean clicked = false;
        Logger.info("Attempting to click element(s) located by: " + locator);

        while (System.currentTimeMillis() < endTime && !clicked) {
            // Find all elements matching the locator
            List<WebElement> elements = driver.findElements(locator);
            if (elements.isEmpty()) {
            	Logger.info("No elements found for locator " + locator + " yet.");
            } else {
            	 Logger.info("Found " + elements.size() + " element(s) for locator " + locator + ". Checking clickability...");
            }

            // Iterate through each found element to find a clickable one
            for (WebElement element : elements) {
                if (!element.isDisplayed()) {
                	Logger.info("Element found but not visible (isDisplayed() == false). Skipping this element.");
                    continue;
                }
                if (!element.isEnabled()) {
                	Logger.info("Element found but not enabled (isEnabled() == false). Skipping this element.");
                    continue;
                }
                // Check if element is not overlapped by another element at its center point
                JavascriptExecutor js = (JavascriptExecutor) driver;
                Boolean isTopElement = (Boolean) js.executeScript(
                    "var elem = arguments[0];" +
                    "var rect = elem.getBoundingClientRect();" +
                    "var cx = rect.left + (rect.width / 2);" +
                    "var cy = rect.top + (rect.height / 2);" +
                    "var topElem = document.elementFromPoint(cx, cy);" +
                    "return (topElem === elem || elem.contains(topElem));",
                    element
                );

                if (isTopElement != null && isTopElement) {
                    // Element is visible, enabled, and not overlapped – safe to click
                	Logger.info("Clicking element: " + locator);
                    element.click();
                    Logger.info("Element clicked successfully.");
                    clicked = true;
                    break;
                } else {
                	Logger.info("Element is overlapped by another element (not top at its center). Skipping this element.");
                    // continue to next element
                }
            }

            if (!clicked) {
                // No element was clickable in this iteration, wait briefly and retry
                try {
                    Thread.sleep(POLLING_INTERVAL_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while waiting for element to become clickable", ie);
                }
                System.out.println("Retrying to find a clickable element for locator: " + locator);
            }
        }
        
        if (!clicked) {
            // Timeout reached without clicking any element
            String errorMsg = "Failed to click any element for locator " + locator 
                              + " after " + timeoutInSeconds + " seconds (no clickable element found).";
            Logger.error(errorMsg);
            throw new TimeoutException(errorMsg);
        }
    }   
    

    public void enterTextInField(String locator, String text) {
        try {
            WebElement element = waitForElement(locator);
            element.clear();
            element.sendKeys(text);
            Logger.info("✅ Entered text: " + text + " in " + locator);
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to enter text in: " + locator, e);
        }
    }

    public void selectDropdownOptionByText(String locator, String value) {
        try {
            Select dropdown = new Select(waitForElement(locator));
            dropdown.selectByVisibleText(value);
            Logger.info("✅ Selected dropdown value: " + value);
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to select dropdown value: " + value, e);
        }
    }
    
    public void typeWithDropdownHandling(String fieldId, String text, String dropdownLocatorCss) throws InterruptedException {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(fieldId)));
        input.clear();

        StringBuilder typed = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            typed.append(c);
            input.sendKeys(String.valueOf(c));
            Thread.sleep(100); // Wait 100ms between keystrokes

            // After 3 characters, wait extra 300ms and check for dropdown
            if (typed.length() == 3) {
                Thread.sleep(300);
                if (isDropdownVisible(dropdownLocatorCss)) {
                    clickFirstDropdownOption(dropdownLocatorCss);
                    return;
                }
            }

            // After every 2 characters, check again (except first 3)
            if (typed.length() > 3 && typed.length() % 2 == 0) {
                if (isDropdownVisible(dropdownLocatorCss)) {
                    clickFirstDropdownOption(dropdownLocatorCss);
                    return;
                }
            }
        }

        // Final check after full input
        Thread.sleep(300);
        if (isDropdownVisible(dropdownLocatorCss)) {
            clickFirstDropdownOption(dropdownLocatorCss);
        }
    }

    private boolean isDropdownVisible(String cssSelector) {
        try {
            List<WebElement> options = driver.findElements(By.cssSelector(cssSelector));
            return !options.isEmpty() && options.get(0).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    private void clickFirstDropdownOption(String cssSelector) {
        List<WebElement> options = driver.findElements(By.cssSelector(cssSelector));
        if (!options.isEmpty()) {
            options.get(0).click();
        }
    }
    

    public boolean isElementVisible(String locator) {
        try {
            driver.findElement(getLocator(locator));
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    
    // true = checked ; false = unchecked
    public void setCheckboxCheckedState(String locator, boolean shouldBeChecked) {
        try {
            WebElement checkbox = waitForElement(locator);
            boolean isChecked = checkbox.isSelected();

            if (shouldBeChecked && !isChecked) {
                checkbox.click();
                Logger.info("✅ Checkbox state updated: Checked");
            } else if(!shouldBeChecked && isChecked) {
            	Logger.info("ℹ️ Checkbox state updated: unchecked: ");
            } else {
            	Logger.info("ℹ️ Checkbox in correct state");
            }
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to set checkbox state: " + locator, e);
        }
    }

    public void focusOnModal(String modalLocator) {
        try {
            WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(getLocator(modalLocator)));
            driver.switchTo().activeElement();
            Logger.info("✅ Switched to modal.");
        } catch (Exception e) {
            throw new RuntimeException("❌ Modal not found: " + modalLocator, e);
        }
    }

    public void closeModalWindow(String closeButtonLocator) {
        try {
            clickElementByLocator(getLocator(closeButtonLocator), timeoutInSeconds * 1000L);
            Logger.info("✅ Modal closed.");
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to close modal.", e);
        }
    }

    public void handleJavaScriptAlert(String action, String inputText) {
        try {
            wait.until(ExpectedConditions.alertIsPresent());
            Alert alert = driver.switchTo().alert();

            switch (action.toLowerCase()) {
                case "accept":
                    alert.accept();
                    Logger.info("✅ Accepted JavaScript alert.");
                    break;
                case "dismiss":
                    alert.dismiss();
                    Logger.info("✅ Dismissed JavaScript alert.");
                    break;
                case "input":
                    alert.sendKeys(inputText);
                    alert.accept();
                    Logger.info("✅ Entered text in JavaScript prompt.");
                    break;
                default:
                    throw new RuntimeException("❌ Invalid JavaScript modal action: " + action);
            }
        } catch (Exception e) {
            throw new RuntimeException("❌ No JavaScript alert found.", e);
        }
    }

    public String getToastPopupMessage(String toastLocator) {
        try {
            WebElement toast = wait.until(ExpectedConditions.visibilityOfElementLocated(getLocator(toastLocator)));
            return toast.getText();
        } catch (Exception e) {
            throw new RuntimeException("❌ Toast message not found: " + toastLocator, e);
        }
    }

    public String getTextFromElement(String locator) {
        try {
            return waitForElement(locator).getText();
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to get text from element: " + locator, e);
        }
    }

    public String captureScreenshot(String screenshotName) {
        try {
            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

            // Align with Hooks.java reportDir
            String reportDir = core.Hooks.getReportDirectory(); // Add a getter for reportDir in Hooks.java
            String screenshotDir = reportDir + "/screenshots";
            new File(screenshotDir).mkdirs();

            String screenshotPath = screenshotDir + "/" + screenshotName + ".png";
            File destFile = new File(screenshotPath);
            Files.copy(srcFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            Logger.info("✅ Screenshot taken: " + screenshotPath);
            return screenshotPath;
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to take screenshot: " + screenshotName, e);
        }
    }

    public void hoverOnElement(String locator) {
        try {
            Actions actions = new Actions(driver);
            actions.moveToElement(waitForElement(locator)).perform();
            Logger.info("✅ Hovered over element: " + locator);
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to hover over: " + locator, e);
        }
    }

    public void doubleClickElement(String locator) {
        try {
            Actions actions = new Actions(driver);
            actions.doubleClick(waitForElement(locator)).perform();
            Logger.info("✅ Double-clicked on: " + locator);
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to double-click: " + locator, e);
        }
    }

    public void rightClickElement(String locator) {
        try {
            Actions actions = new Actions(driver);
            actions.contextClick(waitForElement(locator)).perform();
            Logger.info("✅ Right-clicked on: " + locator);
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to right-click: " + locator, e);
        }
    }

    public String getInputFieldValue(String locator) {
        try {
            WebElement element = waitForElement(locator);
            return element.getDomProperty("value");
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to get input value from: " + locator, e);
        }
    }

    public void clearTextField(String locator) {
        try {
            WebElement element = waitForElement(locator);
            element.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to clear field: " + locator, e);
        }
    }

    public void waitUntilElementVisible(String locator, boolean shouldBeVisible) {
        try {
            if (shouldBeVisible) {
                wait.until(ExpectedConditions.visibilityOfElementLocated(getLocator(locator)));
                Logger.info("✅ Element is visible: " + locator);
            } else {
                wait.until(ExpectedConditions.invisibilityOfElementLocated(getLocator(locator)));
                Logger.info("✅ Element is not visible: " + locator);
            }
        } catch (Exception e) {
            throw new RuntimeException("❌ Timeout waiting for element visibility (" + shouldBeVisible + "): " + locator, e);
        }
    }
    
    public void waitUntilElementVisible(String locator) {
    	waitUntilElementVisible(locator, true);
    }

    public void waitUntilTextAppears(String locator, String expectedText) {
        try {
            wait.until(ExpectedConditions.textToBe(getLocator(locator), expectedText));
            System.out.println("✅ Expected text appeared: " + expectedText);
        } catch (Exception e) {
            throw new RuntimeException("❌ Text did not appear: " + expectedText, e);
        }
    }

    public void scrollToElementByLocator(String locator) {
        try {
            WebElement element = waitForElement(locator);
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
            Logger.info("✅ Scrolled to element: " + locator);
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to scroll to element: " + locator, e);
        }
    }

    public void scrollToWebElement(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
    }

    public void highlightElementBorder(String locator) {
        try {
            WebElement element = waitForElement(locator);
            ((JavascriptExecutor) driver).executeScript("arguments[0].style.border='3px solid red'", element);
            Logger.info("✅ Highlighted element: " + locator);
        } catch (Exception e) {
        	Logger.error("⚠️ Failed to highlight element: " + locator);
        }
    }

    public String getTooltipFromElement(String locator) {
        try {
            WebElement element = waitForElement(locator);
            String tooltipText = element.getDomProperty("title");
            Logger.info("✅ Tooltip text: " + tooltipText);
            return tooltipText;
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to get tooltip from element: " + locator, e);
        }
    }
    
    public boolean elementExists(By selector) {
        try {
            return driver.findElement(selector).isDisplayed();
        } catch (NoSuchElementException | StaleElementReferenceException e) {
            return false;
        }
    }

    public void selectOptionFromDynamicList(String inputLocator, String listItemsLocator, String valueToSelect) {
        try {
            WebElement input = waitForElement(inputLocator);
            input.click();

            wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(getLocator(listItemsLocator)));

            java.util.List<WebElement> items = driver.findElements(getLocator(listItemsLocator));

            boolean found = false;
            for (WebElement item : items) {
                String text = item.getText().trim();
                if (text.equalsIgnoreCase(valueToSelect.trim())) {
                    scrollToWebElement(item);
                    item.click();
                    Logger.info("✅ Selected item: " + text);
                    found = true;
                    break;
                }
            }

            if (!found) {
                throw new RuntimeException("❌ Value not found in list: " + valueToSelect);
            }

        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to select from dynamic list: " + valueToSelect, e);
        }
    }
    
    public void typeAndSelectOption(String inputLocator, String dropdownOptionsLocator, String targetValue, int delayMillis) {
        try {
            WebElement input = waitForElement(inputLocator);
            input.clear();
            boolean found = false;

            for (char ch : targetValue.toCharArray()) {
                input.sendKeys(String.valueOf(ch));
                Thread.sleep(delayMillis);

                java.util.List<WebElement> options = driver.findElements(getLocator(dropdownOptionsLocator));
                for (WebElement option : options) {
                    if (option.getText().trim().equalsIgnoreCase(targetValue.trim())) {
                        scrollToWebElement(option);
                        option.click();
                        Logger.info("✅ Option selected: " + option.getText());
                        found = true;
                        break;
                    }
                }

                if (found) break;
            }

            if (!found) {
                input.sendKeys(Keys.TAB);
                Logger.info("ℹ️ Option not found. Focus moved away from input.");
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("❌ Interrupted while typing and selecting.", e);
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed during type-and-select process.", e);
        }
    }

	public void typeAndSelectOption(String inputLocator, String dropdownOptionsLocator, String targetValue) {
		 typeAndSelectOption(inputLocator,dropdownOptionsLocator,targetValue, 100);
	}
	
	public void uploadFile(String fileInputLocator, String filePath) {
	    try {
	        WebElement fileInput = waitForElement(fileInputLocator);
	        fileInput.sendKeys(new File(filePath).getAbsolutePath());
	        LOGGER.debug("✅ File uploaded: " + filePath);
	    } catch (Exception e) {
	        throw new RuntimeException("❌ Failed to upload file: " + filePath, e);
	    }
	}
	
	public void uploadHiddenFile(String fileInputLocator, String filePath) {
	    try {
	        WebElement fileInput = driver.findElement(getLocator(fileInputLocator));
	        ((JavascriptExecutor) driver).executeScript("arguments[0].style.display = 'block';", fileInput);
	        fileInput.sendKeys(new File(filePath).getAbsolutePath());
	        LOGGER.debug("✅ Hidden file input uploaded: " + filePath);
	    } catch (Exception e) {
	        throw new RuntimeException("❌ Failed to upload via hidden input: " + filePath, e);
	    }
	}
	
	public void uploadFileViaDragAndDrop(String dropZoneLocator, String filePath) {
	    try {
	        WebElement dropZone = waitForElement(dropZoneLocator);
	        File file = new File(filePath);
	        String absolutePath = file.getAbsolutePath();

	        JavascriptExecutor js = (JavascriptExecutor) driver;

	        String jsDropFile =
	            "var target = arguments[0]," +
	            "    offsetX = 0," +
	            "    offsetY = 0," +
	            "    document = target.ownerDocument || document," +
	            "    window = document.defaultView || window;" +
	            "" +
	            "var input = document.createElement('INPUT');" +
	            "input.type = 'file';" +
	            "input.style.display = 'none';" +
	            "input.onchange = function () {" +
	            "  var rect = target.getBoundingClientRect()," +
	            "      x = rect.left + (offsetX || (rect.width >> 1))," +
	            "      y = rect.top + (offsetY || (rect.height >> 1))," +
	            "      dataTransfer = { files: this.files };" +
	            "" +
	            "  ['dragenter', 'dragover', 'drop'].forEach(function (name) {" +
	            "    var evt = document.createEvent('MouseEvent');" +
	            "    evt.initMouseEvent(name, true, true, window, 0, 0, 0, x, y, false, false, false, false, 0, null);" +
	            "    evt.dataTransfer = dataTransfer;" +
	            "    target.dispatchEvent(evt);" +
	            "  });" +
	            "  setTimeout(function () { document.body.removeChild(input); }, 25);" +
	            "};" +
	            "document.body.appendChild(input);" +
	            "return input;";

	        WebElement input = (WebElement) js.executeScript(jsDropFile, dropZone);
	        input.sendKeys(absolutePath);

	        LOGGER.debug("✅ Drag-and-drop file upload triggered: " + filePath);
	    } catch (Exception e) {
	        throw new RuntimeException("❌ Failed drag-and-drop upload for file: " + filePath, e);
	    }
	}
	
	public void assertValue(String pageName, String fieldName, String expectedValue, String actualValue, boolean testType) {
		if (testType) {
		    try {
		        assertTrue("On Page '" + pageName+ "' for Field: '"+ fieldName +"' Expected: '" + expectedValue + "' Actual: '" + actualValue + "'", expectedValue.equals(actualValue));
		        Hooks.getExtentTest().pass("✅ Navigated and verified passed for field: " + fieldName);
		    } catch (AssertionError ae) {
		        Hooks.getExtentTest().fail("❌ Assertion failed: " + ae.getMessage());
		        throw ae; // re-throw so the test still fails
		    }	
		} else {
		    try {
		        assertFalse("On Page '" + pageName+ "' for Field: '"+ fieldName +"' Should not match Expected: '" + expectedValue + "' Actual: '" + actualValue + "'", expectedValue.equals(actualValue));
		        Hooks.getExtentTest().pass("✅ Navigated and verified passed for field: " + fieldName);
		    } catch (AssertionError ae) {
		        Hooks.getExtentTest().fail("❌ Assertion failed: " + ae.getMessage());
		        throw ae; // re-throw so the test still fails
		    }	
		}
	}
	
	public void assertCheckboxSelected(String locator, boolean shouldBeSelected) {
	    WebElement checkbox = waitForElement(locator);
	    boolean isSelected = checkbox.isSelected();
	    if (shouldBeSelected) {
	        assertTrue("❌ Checkbox should be selected: " + locator, isSelected);
	        LOGGER.debug("✅ Checkbox is selected: " + locator);
	    } else {
	        assertFalse("❌ Checkbox should not be selected: " + locator, isSelected);
	        LOGGER.debug("✅ Checkbox is not selected: " + locator);
	    }
	}
	
	public void assertRadioButtonSelected(String locator, boolean shouldBeSelected) {
	    WebElement radioButton = waitForElement(locator);
	    boolean isSelected = radioButton.isSelected();
	    if (shouldBeSelected) {
	        assertTrue("❌ Radio button should be selected: " + locator, isSelected);
	        LOGGER.debug("✅ Radio button is selected: " + locator);
	    } else {
	        assertFalse("❌ Radio button should not be selected: " + locator, isSelected);
	        LOGGER.debug("✅ Radio button is not selected: " + locator);
	    }
	}
	
	
	public void assertValue(String pageName, String fieldName, String expectedValue, String actualValue) {
		assertValue(pageName,fieldName,expectedValue,actualValue, true);
	}
	
	public void checkElementProperty(String xpath, String attributeName, String expectedValue) {
	    try {
	        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));

	        // Wait until the attribute has the expected value
	        wait.until(ExpectedConditions.attributeToBe(element, attributeName, expectedValue));

	        String actualValue = element.getAttribute(attributeName);
	        assertTrue("❌ Expected attribute '" + attributeName + "' to be '" + expectedValue 
	                   + "', but was: '" + actualValue + "'", expectedValue.equalsIgnoreCase(actualValue));
	        Logger.info("✅ Attribute '" + attributeName + "' has expected value: " + expectedValue);
	    } catch (TimeoutException e) {
	        throw new AssertionError("❌ Timeout: attribute '" + attributeName + "' did not become '" 
	                                 + expectedValue + "' for element: " + xpath, e);
	    } catch (Exception e) {
	        throw new RuntimeException("❌ Failed to verify attribute '" + attributeName + "' for: " + xpath, e);
	    }
	}
	
	// 🔍 Shadow DOM Support

	public WebElement expandShadowRoot(WebElement shadowHost) {
	    try {
	        JavascriptExecutor js = (JavascriptExecutor) driver;
	        return (WebElement) js.executeScript("return arguments[0].shadowRoot", shadowHost);
	    } catch (Exception e) {
	        throw new RuntimeException("❌ Failed to expand shadow root for host element", e);
	    }
	}

	public WebElement findElementInShadowRoot(String hostLocator, String shadowCssSelector) {
	    try {
	        WebElement host = waitForElement(hostLocator);
	        WebElement shadowRoot = expandShadowRoot(host);
	        return shadowRoot.findElement(By.cssSelector(shadowCssSelector));
	    } catch (Exception e) {
	        throw new RuntimeException("❌ Failed to find element in shadow DOM: " + shadowCssSelector, e);
	    }
	}

	// 🖼️ iFrame Support

	public void switchToIFrame(String locator) {
	    try {
	        WebElement iframe = waitForElement(locator);
	        driver.switchTo().frame(iframe);
	        LOGGER.debug("✅ Switched to iFrame: " + locator);
	    } catch (Exception e) {
	        throw new RuntimeException("❌ Failed to switch to iFrame: " + locator, e);
	    }
	}

	public void switchToDefaultContent() {
	    try {
	        driver.switchTo().defaultContent();
	        LOGGER.debug("✅ Switched to default content");
	    } catch (Exception e) {
	        throw new RuntimeException("❌ Failed to switch to default content", e);
	    }
	}

}


