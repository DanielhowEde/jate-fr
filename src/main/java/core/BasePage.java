/*
 * © 2025 Daniel Ede. This code is licensed for personal or educational use only.
 * Unauthorized reproduction or distribution is prohibited.
 * Not text Selector = locator once in webElement form referred to as Selector
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

    private By getSelector(String locator) {
        String trimmed = locator.trim();
        if (trimmed.startsWith("/") || trimmed.startsWith("(")) {
            return By.xpath(trimmed);
        }
        return By.cssSelector(trimmed);
    }

    protected WebElement waitForSelector(String locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(getSelector(locator)));
    }
    
    protected WebElement waitForElement(WebElement Selector) {
        return wait.until(ExpectedConditions.elementToBeClickable(Selector));
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

    public void scrollToWebElement(WebElement element) {
        ((JavascriptExecutor) driver).executeScript(
            "const el = arguments[0];" +
            "el.scrollIntoView({behavior: 'instant', block: 'center', inline: 'nearest'});" +
            "const rect = el.getBoundingClientRect();" +
            "if (rect.height === 0 || rect.width === 0 || rect.bottom < 0 || rect.top > window.innerHeight) {" +
            "  throw 'Element not visible in viewport after scroll';" +
            "}", 
            element
        );
    }
    
    public void selectDateFromCalendar(String dateFieldSelector, String date) {
        try {
            WebElement dateField = waitForSelector(dateFieldSelector);
            dateField.click();
            dateField.clear();
            dateField.sendKeys(date);
            dateField.sendKeys(Keys.TAB); // Close the calendar if needed
            Logger.info("✅ Date entered: " + date);
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to set date in calendar: " + dateFieldSelector, e);
        }
    }
    
    private void clickElementBySelector(By selector, long timeoutMs) {
        long endTime = System.currentTimeMillis() + timeoutMs;
        boolean clicked = false;
        LOGGER.debug("Attempting to click element(s) located by: " + selector);

        while (System.currentTimeMillis() < endTime && !clicked) {
            // Find all elements matching the locator
            List<WebElement> elements = driver.findElements(selector);
            if (elements.isEmpty()) {
            	 LOGGER.debug("No elements found for locator " + selector + " yet.");
            } else {
            	 LOGGER.info("Found " + elements.size() + " element(s) for locator " + selector + ". Checking clickability...");
            }

            // Iterate through each found element to find a clickable one
            for (WebElement element : elements) {
                // Scroll element into view using JavaScript
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);

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
                    LOGGER.info("Clicking element: " + selector);
                    element.click();
                    LOGGER.info("Element clicked successfully.");
                    clicked = true;
                    break;
                } else {
                    LOGGER.debug("Element is overlapped by another element (not top at its center). Skipping this element.");
                }
            }
        }

        if (!clicked) {
            // Timeout reached without clicking any element
            String errorMsg = "Failed to click any element for locator " + selector 
                              + " after " + timeoutMs + " ms (no clickable element found).";
            LOGGER.error(errorMsg);
            throw new TimeoutException(errorMsg);
        }
    }

    public void clickElementBySelector(String thisSelector) {
    	By selector = getSelector(thisSelector);
        long endTime = System.currentTimeMillis() + timeoutInSeconds * 1000L;
        clickElementBySelector(selector, endTime);
    }   
    

    public void enterTextInField(String locator, String text) {
        try {
            WebElement selector = waitForSelector(locator);
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", selector);

            // Attempt 1
            selector.click();
            selector.clear();
            selector.sendKeys(text);

            if (!textAppeared(selector, text)) {
                Logger.info("⌛ Waiting 5s to recheck field content...");
                Thread.sleep(5000);

                if (!textAppeared(selector, text)) {
                    Logger.warn("⚠️ Text not stable in field. Retrying input...");

                    // Retry
                    selector.click();
                    selector.clear();
                    selector.sendKeys(text);

                    if (!textAppeared(selector, text)) {
                        throw new RuntimeException("❌ Text input retry failed. Expected: '" + text +
                                "', but found: '" + selector.getDomProperty("value") + "'");
                    }
                }
            }

            Logger.info("✅ Entered text: '" + text + "' successfully in " + locator);
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to enter text in: " + locator, e);
        }
    }

    private boolean textAppeared(WebElement element, String expectedText) {
        String actual = element.getDomProperty("value");
        return actual != null && actual.trim().equals(expectedText.trim());
    }

    public void selectDropdownOptionByText(String locator, String value) {
        try {
            Select selector = new Select(waitForSelector(locator));
            selector.selectByVisibleText(value);
            Logger.info("✅ Selected dropdown value: " + value);
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to select dropdown value: " + value, e);
        }
    }
    
    public void typeWithDropdownHandling(String locator, String text, String dropdownLocator, int delay) throws InterruptedException {
        typeIntoFieldAndSelect(locator, text, dropdownLocator, delay, false);
    }

    private boolean isDropdownVisible(String dropdownLocator) {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(getSelector(dropdownLocator))).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    private void clickFirstDropdownOption(String dropdownLocator) {
        WebElement firstOption = wait.until(ExpectedConditions.elementToBeClickable(
            getSelector(dropdownLocator)
        ));
        firstOption.click();
    }
    

    public boolean isElementVisible(String locator) {
        try {
            driver.findElement(getSelector(locator));
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }
    
    public boolean isElementVisible(WebElement element) {
        try {
            return element != null && element.isDisplayed();
        } catch (StaleElementReferenceException | NoSuchElementException e) {
            return false;
        }
    }

    
    // true = checked ; false = unchecked
    public void setCheckboxCheckedState(String locator, boolean shouldBeChecked) {
        try {
            WebElement checkbox = waitForSelector(locator);
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
            WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(getSelector(modalLocator)));
            driver.switchTo().activeElement();
            Logger.info("✅ Switched to modal.");
        } catch (Exception e) {
            throw new RuntimeException("❌ Modal not found: " + modalLocator, e);
        }
    }

    public void closeModalWindow(String closeButtonLocator) {
        try {
            clickElementBySelector(getSelector(closeButtonLocator), timeoutInSeconds * 1000L);
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
            WebElement toast = wait.until(ExpectedConditions.visibilityOfElementLocated(getSelector(toastLocator)));
            return toast.getText();
        } catch (Exception e) {
            throw new RuntimeException("❌ Toast message not found: " + toastLocator, e);
        }
    }

    public String getTextFromElement(String locator) {
        try {
            return waitForSelector(locator).getText();
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
    
    public void captureAndAttachScreenshot(String note) {
        String screenshotName = "Manual_" + note.replaceAll("[^a-zA-Z0-9]", "_");
        String path = captureScreenshot(screenshotName);

        Hooks.getExtentTest()
            .info("📸 " + note)
            .addScreenCaptureFromPath("screenshots/" + new File(path).getName());
    }

    public void hoverOnElement(String locator) {
        try {
            Actions actions = new Actions(driver);
            actions.moveToElement(waitForSelector(locator)).perform();
            Logger.info("✅ Hovered over element: " + locator);
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to hover over: " + locator, e);
        }
    }

    public void doubleClickElement(String locator) {
        try {
            Actions actions = new Actions(driver);
            actions.doubleClick(waitForSelector(locator)).perform();
            Logger.info("✅ Double-clicked on: " + locator);
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to double-click: " + locator, e);
        }
    }

    public void rightClickElement(String locator) {
        try {
            Actions actions = new Actions(driver);
            actions.contextClick(waitForSelector(locator)).perform();
            Logger.info("✅ Right-clicked on: " + locator);
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to right-click: " + locator, e);
        }
    }

    public String getInputFieldValue(String locator) {
        try {
            WebElement element = waitForSelector(locator);
            return element.getDomProperty("value");
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to get input value from: " + locator, e);
        }
    }

    public void clearTextField(String locator) {
        try {
            WebElement element = waitForSelector(locator);
            element.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to clear field: " + locator, e);
        }
    }

    public void waitUntilElementVisible(String locator, boolean shouldBeVisible) {
        try {
            if (shouldBeVisible) {
                wait.until(ExpectedConditions.visibilityOfElementLocated(getSelector(locator)));
                Logger.info("✅ Element is visible: " + locator);
            } else {
                wait.until(ExpectedConditions.invisibilityOfElementLocated(getSelector(locator)));
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
            wait.until(ExpectedConditions.textToBe(getSelector(locator), expectedText));
            LOGGER.info("✅ Expected text appeared: " + expectedText);
        } catch (Exception e) {
        	LOGGER.error("✘ Text did not appear within Expected TimeLimit");
            throw new RuntimeException("❌ Text did not appear: " + expectedText, e);
        }
    }

    public void scrollToSelector(String locator) {
        try {
            WebElement element = waitForSelector(locator);
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
            Logger.info("✅ Scrolled to element: " + locator);
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to scroll to element: " + locator, e);
        }
    }

    public void scrollToIfPresent(String locator) {
        try {
            WebElement element = driver.findElement(getSelector(locator));
            if (element != null && !isElementInViewport(element)) {
                scrollToWebElement(element);
                Logger.info("✅ Scrolled to element that was off-screen: " + locator);
            }
        } catch (NoSuchElementException e) {
            Logger.warn("⚠️ Element not found for scrolling: " + locator);
        }
    }

    public void highlightElementBorder(String locator) {
        try {
            WebElement element = waitForSelector(locator);
            ((JavascriptExecutor) driver).executeScript("arguments[0].style.border='3px solid red'", element);
            Logger.info("✅ Highlighted element: " + locator);
        } catch (Exception e) {
        	Logger.error("⚠️ Failed to highlight element: " + locator);
        }
    }

    public String getTooltipFromElement(String locator) {
        try {
            WebElement element = waitForSelector(locator);
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
            WebElement input = waitForSelector(inputLocator);
            input.click();

            wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(getSelector(listItemsLocator)));

            java.util.List<WebElement> items = driver.findElements(getSelector(listItemsLocator));

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
    
    public void typeAndSelectOption(String locator, String text, int delay) {
        try {
            String dropdownXPath = "//*[starts-with(@class, 'oj-dynamic-table') and contains(text(), '" + text + "')]";
            typeIntoFieldAndSelect(locator, text, dropdownXPath, delay, true);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("❌ Interrupted while typing and selecting.", e);
        }
    }

	public void typeAndSelectOption(String inputLocator, String textValue) {
		 typeAndSelectOption(inputLocator,textValue, 200);
	}
	
	public void uploadFile(String fileInputLocator, String filePath) {
	    try {
	        WebElement fileInput = waitForSelector(fileInputLocator);
	        fileInput.sendKeys(new File(filePath).getAbsolutePath());
	        LOGGER.debug("✅ File uploaded: " + filePath);
	    } catch (Exception e) {
	        throw new RuntimeException("❌ Failed to upload file: " + filePath, e);
	    }
	}
	
	public void uploadHiddenFile(String fileInputLocator, String filePath) {
	    try {
	        WebElement fileInput = driver.findElement(getSelector(fileInputLocator));
	        ((JavascriptExecutor) driver).executeScript("arguments[0].style.display = 'block';", fileInput);
	        fileInput.sendKeys(new File(filePath).getAbsolutePath());
	        LOGGER.debug("✅ Hidden file input uploaded: " + filePath);
	    } catch (Exception e) {
	        throw new RuntimeException("❌ Failed to upload via hidden input: " + filePath, e);
	    }
	}
	
	public void uploadFileViaDragAndDrop(String dropZoneLocator, String filePath) {
	    try {
	        WebElement dropZone = waitForSelector(dropZoneLocator);
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
	    WebElement checkbox = waitForSelector(locator);
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
	    WebElement radioButton = waitForSelector(locator);
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

	        String actualValue = element.getDomProperty(attributeName);
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
	        WebElement host = waitForSelector(hostLocator);
	        WebElement shadowRoot = expandShadowRoot(host);
	        return shadowRoot.findElement(By.cssSelector(shadowCssSelector));
	    } catch (Exception e) {
	        throw new RuntimeException("❌ Failed to find element in shadow DOM: " + shadowCssSelector, e);
	    }
	}

	// 🖼️ iFrame Support

	public void switchToIFrame(String locator) {
	    try {
	        WebElement iframe = waitForSelector(locator);
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
	
	public void selectRadioButton(String locator) {
	    try {
	        WebElement selector = waitForSelector(locator);

	        if (!selector.isSelected()) {
	            scrollToWebElement(selector);
	            selector.click();
	            Logger.info("✅ Radio button selected: " + locator);
	        } else {
	            Logger.info("ℹ️ Radio button already selected: " + locator);
	        }
	    } catch (Exception e) {
	        throw new RuntimeException("❌ Failed to select radio button: " + locator, e);
	    }
	}
	
	private void typeIntoFieldAndSelect(String inputLocator, String fullText, String dropdownLocator, int delay, boolean checkAllMatches) throws InterruptedException {
	    WebElement input = waitForSelector(inputLocator);
	    By dropdownSelector = getSelector(dropdownLocator);

	    int attempt = 0;
	    boolean success = false;

	    while (attempt < 3 && !success) {
	        input.click();
	        input.clear();
	        input.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);

	        StringBuilder typed = new StringBuilder();

	        for (int i = 0; i < fullText.length(); i++) {
	            char c = fullText.charAt(i);
	            typed.append(c);
	            input.sendKeys(String.valueOf(c));
	            Thread.sleep(100);

	            if (typed.length() >= 3 && (typed.length() == 3 || typed.length() % 2 == 0)) {
	                Thread.sleep(delay);

	                if (checkAllMatches) {
	                    List<WebElement> matches = driver.findElements(dropdownSelector);
	                    for (WebElement match : matches) {
	                        String visibleText = match.getText().trim();
	                        if (visibleText.equalsIgnoreCase(fullText)) {
	                            scrollToWebElement(match);
	                            match.click();
	                            Logger.info("✅ Option selected: " + visibleText);
	                            success = true;
	                            break;
	                        }
	                    }
	                } else {
	                    WebElement firstOption = getFirstVisibleElement(dropdownSelector);
	                    if (firstOption != null) {
	                        scrollToWebElement(firstOption);
	                        firstOption.click();
	                        Logger.info("✅ First dropdown option selected: " + firstOption.getText());
	                        success = true;
	                        break;
	                    }
	                }
	            }
	        }

	        String actual = input.getDomProperty("value");
	        if (actual != null && actual.trim().equalsIgnoreCase(fullText.trim())) {
	            success = true;
	        } else {
	            Logger.warn("⚠️ Text mismatch after attempt " + (attempt + 1) + ": expected '" + fullText + "', found '" + actual + "'");
	            Thread.sleep(2000);
	        }

	        attempt++;
	    }

	    if (!success) {
	        Logger.warn("⚠️ Final attempt fallback: pressing TAB to continue without dropdown selection.");
	        input.sendKeys(Keys.TAB);
	    } else {
	        Logger.info("✅ Final text value accepted: " + fullText);
	    }
	}

	private WebElement getFirstVisibleElement(By selector) {
	    List<WebElement> elements = driver.findElements(selector);
	    for (WebElement el : elements) {
	        if (isElementVisible(el)) return el;
	    }
	    return null;
	}
	
	private boolean isElementInViewport(WebElement element) {
	    JavascriptExecutor js = (JavascriptExecutor) driver;
	    String script = """
	        var rect = arguments[0].getBoundingClientRect();
	        return (
	            rect.top >= 0 &&
	            rect.left >= 0 &&
	            rect.bottom <= (window.innerHeight || document.documentElement.clientHeight) &&
	            rect.right <= (window.innerWidth || document.documentElement.clientWidth)
	        );
	    """;
	    return (Boolean) js.executeScript(script, element);
	}
}



