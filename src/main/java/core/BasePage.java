/*
 * © 2025 Daniel Ede. This code is licensed for personal or educational use only.
 * Unauthorized reproduction or distribution is prohibited.
 * Not text Selector = locator once in webElement form referred to as Selector
 */
package core;

import org.jcodec.common.logging.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;


import utils.context.*;
import utils.config.ConfigReader;
import utils.form.FormFieldType;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static core.TestLogger.LOGGER;

import java.io.File;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

public class BasePage {
    protected WebDriver driver;
    protected int timeoutInSeconds;
    private WebDriverWait wait;
    private static final long POLLING_INTERVAL_MS = 500;
    private String originalTab;
    private WebElement currentModal = null;
    private static final AtomicInteger screenshotCounter = new AtomicInteger(0);
    
    protected <T> T waitFor(ExpectedCondition<T> condition) {
        try {
            return wait.until(condition);
        } catch (RuntimeException e) {
            ScenarioContext scenarioContext = new ScenarioContext();
			scenarioContext.set("LAST_EXCEPTION", e);
            throw e;
        }
    }
      
    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.originalTab = driver.getWindowHandle();
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
    
    private SearchContext getSearchContext() {
    	return (currentModal != null) ? currentModal : driver;
    }
    
    public boolean isModalActive() {
    	return currentModal != null;
    }
    
    public static void resetScreenshotCounter() {
    	screenshotCounter.set(0);
    }

    public static int getNextScreenshotNumber() {
    	return screenshotCounter.incrementAndGet();
    }

    public void clearModalCurrent() {
    	currentModal= null;
    	LOGGER.info("Modal Context cleared");
    }

    private By getSelector(String locator) {
        String trimmed = locator.trim();
        if (trimmed.startsWith("/") || trimmed.startsWith("(")) {
            return By.xpath(trimmed);
        }
        return By.cssSelector(trimmed);
    }

    protected WebElement waitForSelector(String locator) {
    	By by = getSelector(locator);
    	if (currentModal == null) {
    		return wait.until(ExpectedConditions.elementToBeClickable(by));
    	}
    	
        return new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds)).until(d -> {
        	try {
        		WebElement wEl =currentModal.findElement(by);
        		if (wEl.isDisplayed() && wEl.isEnabled()) {
        			return wEl;
        		}
        		return null;
        	} catch (StaleElementReferenceException | NoSuchElementException ex) {
        		return null;
        	}
        });
    }
    
    protected WebElement waitForElement(WebElement Selector) {
        return wait.until(ExpectedConditions.elementToBeClickable(Selector));
    }

    public boolean isElementOnTop(WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        String script = """
            var elem = arguments[0];
            var rect = elem.getBoundingClientRect();
            var elFromPoint = document.elementFromPoint(rect.left + rect.width/2, rect.top + rect.height/2);
            return elem === elFromPoint || elem.contains(elFromPoint);
        """;
        return (Boolean) js.executeScript(script, element);
    }

    public void navigateTo(String url) {
        LOGGER.info("Navigating to URL: " + url);
        driver.get(url);
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
        	//convert excel number to date
  
        	String formattedDate = convertNumberToDate(date);
            WebElement dateField = waitForSelector(dateFieldSelector);
            dateField.click();
            dateField.clear();
            dateField.sendKeys(formattedDate);
            dateField.sendKeys(Keys.TAB); // Close the calendar if needed
            Logger.info("✅ Date entered: " + date);
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to set date in calendar: " + dateFieldSelector, e);
        }
    }
    
    public void sleepFor(int sleepTime) throws Exception {
    	Thread.sleep(sleepTime*1000);
    }
    
    public void mouseSelectDateFromCalendar(String dateFieldSelector, String date) {
        try {
        	//convert excel number to date
  
        	String formattedDate = convertNumberToDate(date);
            WebElement dateField = waitForSelector(dateFieldSelector);

    	    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center', inline: 'nearest'});", dateField);
    	    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", dateField);
            dateField.clear();
            dateField.sendKeys(formattedDate);
            dateField.sendKeys(Keys.TAB); // Close the calendar if needed
            Logger.info("✅ Date entered: " + date);
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to set date in calendar: " + dateFieldSelector, e);
        }
    }
    
    public String convertNumberToDate(String date) {
       	//convert excel number to date
    	int serial= Integer.parseInt(date)-1;
    	LocalDate baseDate = LocalDate.of(1899, 12, 31);
    	LocalDate thisDate= baseDate.plusDays(serial);
    	DateTimeFormatter formatter= DateTimeFormatter.ofPattern("dd/MM/yyyy");
    	return thisDate.format(formatter);
    }
    
    private void clickElementBySelector(By selector, long timeoutMs) {
        long endTime = System.currentTimeMillis() + timeoutMs;
        boolean clicked = false;
        LOGGER.debug("Attempting to click element(s) located by: " + selector);

        while (System.currentTimeMillis() < endTime && !clicked) {
            List<WebElement> elements = driver.findElements(selector);
            if (elements.isEmpty()) {
                LOGGER.debug("No elements found for locator " + selector + " yet.");
            } else {
                LOGGER.info("Found " + elements.size() + " element(s) for locator " + selector + ". Checking clickability...");
            }

            for (WebElement element : elements) {
                JavascriptExecutor js = (JavascriptExecutor) driver;

                // Check if element is in viewport
                boolean inViewport = (Boolean) js.executeScript(
                    "var elem = arguments[0], box = elem.getBoundingClientRect();" +
                    "return (box.top >= 0 && box.left >= 0 && box.bottom <= (window.innerHeight || document.documentElement.clientHeight) &&" +
                    "box.right <= (window.innerWidth || document.documentElement.clientWidth));",
                    element
                );

                if (!inViewport) {
                    LOGGER.debug("Element is outside viewport. Scrolling to bring it into view.");
                    js.executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
                }

                if (!element.isDisplayed()) {
                    LOGGER.debug("Element found but not visible (isDisplayed() == false). Skipping this element.");
                    continue;
                }

                if (!element.isEnabled()) {
                    LOGGER.debug("Element found but not enabled (isEnabled() == false). Skipping this element.");
                    continue;
                }

                // Check if element is not overlapped
                Boolean isTopElement = (Boolean) js.executeScript(
                    "var elem = arguments[0];" +
                    "var rect = elem.getBoundingClientRect();" +
                    "var cx = rect.left + (rect.width / 2);" +
                    "var cy = rect.top + (rect.height / 2);" +
                    "var topElem = document.elementFromPoint(cx, cy);" +
                    "return (topElem === elem || elem.contains(topElem));",
                    element
                );

                if (Boolean.TRUE.equals(isTopElement)) {
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
            LOGGER.warn("Failed to click element: " + selector + " within timeout of " + timeoutMs + "ms.");
        }
    }
    
    public String getVisibleText(String locatorMain, String LocatorSub) {
    	WebElement mainHeader = driver.findElements(By.xpath(locatorMain))
    		    .stream()
    		    .filter(WebElement::isDisplayed)
    		    .findFirst()
    		    .orElseThrow(() -> new RuntimeException("No Visible Element found: " + locatorMain));

    		WebElement subItem = mainHeader.findElements(By.xpath(LocatorSub))  // Use findElements instead of findElement
    		    .stream()
    		    .filter(WebElement::isDisplayed)
    		    .findFirst()
    		    .orElseThrow(() -> new RuntimeException("No Visible Sub Element found: " + locatorMain));
    		
    		return subItem.getText().trim();
    		
    }

    public void clickElementBySelector(String thisSelector) {
    	By selector = getSelector(thisSelector);
        long endTime = System.currentTimeMillis() + timeoutInSeconds * 1000L;
        clickElementBySelector(selector, endTime);
    }   
    
    public void enterTextInFieldViaAction(String locator,String text) {
    	Actions actions = new Actions(driver);
    	
    	WebElement element = driver.findElement(getSelector(locator));
    	actions.moveToElement(element)
    		.click()
    		.keyDown(Keys.CONTROL).sendKeys("a").keyUp(Keys.CONTROL)
    		.sendKeys(Keys.DELETE)
    		.sendKeys(text)
    		.perform();
    }

    public void enterTextInField(String locator, String text) {
    	if (text!=null && !text.isBlank()) {
	        try {
	            WebElement selector = waitForSelector(locator);
	            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", selector);
	
	            // Attempt 1
	            try {
	                selector.click();
	            } catch (Exception clickException) {
	                Logger.warn("⚠️ Standard click failed, trying JS click for: " + locator);
	                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", selector);
	            }

	            // Clear and send keys
	            selector.clear();
	            selector.sendKeys(text);

	            Logger.info("✅ Entered text: '" + text + "' successfully in " + locator);
	        } catch (Exception e) {
	            throw new RuntimeException("❌ Failed to enter text in: " + locator, e);
	        }
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
    
    public void selectDropDownOptionByClicks(String locator, String value) {
    	WebElement selector = waitForSelector(locator);
    	selector.click();
    	String dropdownSelector= String.format("//*[@title='%s']", value);
    	WebElement dropdownValue = waitForSelector(dropdownSelector);
    	dropdownValue.click();
    	
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

    public boolean checkTextWithinFields(String locator, String text) {
    	List<WebElement> divs = driver.findElements(getSelector(locator));
    	boolean hasText = false;
    	String ThisText = "";
    	for (WebElement div: divs) {
    		ThisText = div.getText().trim();
    		if (div != null && div.getText() != null && div.getText().trim().contains(text)){
    			hasText = true;
    		}
    	}
    	
    	return hasText;
    	
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
        	By by = getSelector(modalLocator);
            currentModal = wait.until(ExpectedConditions.visibilityOfElementLocated(by));
            driver.switchTo().activeElement();
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
    
    public void sendCTRLKeyToSelector(String selector, Keys sendKeyPress) {
    	WebElement input = new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.elementToBeClickable(By.xpath(selector)));
    	input.click();
    	input.sendKeys(Keys.END);
    	input.sendKeys(sendKeyPress);
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
        	int number = getNextScreenshotNumber();
        	
            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

            // Align with Hooks.java reportDir
            String reportDir = core.Hooks.getReportDirectory(); // Add a getter for reportDir in Hooks.java
            String screenshotDir = reportDir + "/screenshots";
            new File(screenshotDir).mkdirs();

            String screenshotPath = screenshotDir + "/" +number+ "_"+ screenshotName + ".png";
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
    
    public void clickUsingJS(String locator) {
    	WebElement wElement = wait.until(ExpectedConditions.elementToBeClickable(getSelector(locator)));
    	((JavascriptExecutor)driver).executeScript("arguments[0].click();", wElement);
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
    
    public void waitUntilElementClickable(String locator) throws Exception {
    	 wait.until(ExpectedConditions.elementToBeClickable(getSelector(locator)));
	}
    
    public void waitUntilElementVisible(String locator) {
    	waitUntilElementVisible(locator, true);
    }
    
    public void shortWait(int durationSeconds) throws Exception {
    	Thread.sleep(durationSeconds*1000L);
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
    
    public void scrollToSelectorModal(String locator) {
    	WebElement modalWindow = driver.findElement(By.xpath("//*[@class='modal-body scrollable slds-modal__content slds-p-around_medium']"));
        WebElement element = modalWindow.findElement(getSelector(locator));
        
        new Actions(driver).moveToElement(element).perform(); 
        
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
    
    public boolean elementExists(String locator) {
        try {
        	By selector = getSelector(locator);
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
	
	 // Open a new tab and navigate to a URL
    public void openNewTab(String url) throws InterruptedException {
        driver.switchTo().newWindow(WindowType.TAB);
        Thread.sleep(1000);
        driver.get(url);
        Logger.info("New Tab opened for URL: "+ url);
    }

    
    // Switch to tab by index (0 = original)
    public void switchToTab(int index) {
        List<String> tabs = new ArrayList<>(driver.getWindowHandles());
        if (index < tabs.size()) {
            driver.switchTo().window(tabs.get(index));
            Logger.info("Switched to Tab: "+ index);
        } else {
            throw new IllegalArgumentException("Tab index out of bounds: " + index);
        }
    }
    // Switch back to the original tab
    public void switchToOriginalTab() {
        driver.switchTo().window(originalTab);
    }

    // Close the current tab and switch to a specified one
    public void closeCurrentTabAndSwitchTo(int index) {
        driver.close();
        switchToTab(index);
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
	
	public void selectMenuOption(String menuOption, String Option) throws Exception {
	
		waitUntilElementVisible(menuOption);
    	clickElementBySelector(menuOption);
        Thread.sleep(500);
        WebElement logOut= driver.findElement(By.xpath(Option));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", logOut);
        Thread.sleep(1000);
	}
	
	public static WebElement waitForPresence(WebDriver driver, int timeoutInSeconds, By... locators) {
	    if (locators == null || locators.length == 0) {
	        throw new IllegalArgumentException("At least one locator must be provided.");
	    }

	    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));

	    try {
	        return wait.until(driver1 -> {
	            for (By locator : locators) {
	                List<WebElement> elements = driver1.findElements(locator);
	                if (!elements.isEmpty()) {
	                    return elements.get(0);
	                }
	            }
	            return null;
	        });
	    } catch (TimeoutException e) {
	        throw new TimeoutException("None of the expected elements were found within " + timeoutInSeconds + " seconds.", e);
	    }
	}
	
	
	public void clickOptions(String locator) throws Exception {
		WebElement thisOption= driver.findElement(By.xpath(locator));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", thisOption);
        Thread.sleep(500);
	}
	
	public void mouseOverAndClick(String locator) throws Exception {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
		WebElement screenObject = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(locator)));
		((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center', inline: 'nearest'});", screenObject);
		Actions actions = new Actions(driver);
		actions.moveToElement(screenObject).click().perform();
	}
	
	public void mouseOverAndClickCheckBox(String locator) throws Exception {
	    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
	    WebElement screenObject = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(locator)));
	    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center', inline: 'nearest'});", screenObject);
	    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", screenObject);
	}
	
	
	public void editCasedropdownScrollAndClick(String dropdownXpath, String optionXpath) throws Exception {
	    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

	    WebElement dropdown = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(dropdownXpath)));
	    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", dropdown);
	    dropdown.click();
	    WebElement option = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(optionXpath)));

	    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", option);
	    option.click();
	}
	
	public void dropdownScrollAndClick(String locatorToMouseOver, String locatorToBeFound) throws Exception {
	    WebElement dropdownTrigger = waitForSelector(locatorToMouseOver);

	    // Scroll the dropdown trigger into view
	    ((JavascriptExecutor) driver).executeScript(
	        "arguments[0].scrollIntoView({block: 'center', inline: 'nearest'});", dropdownTrigger);

	    // Click to open the dropdown
	    Actions actions = new Actions(driver);
	    actions.moveToElement(dropdownTrigger).click().perform();

	    int attempt = 0;
	    while (attempt < 20) {
	        try {
	            List<WebElement> options;

	            if (currentModal == null) {
	                options = driver.findElements(getSelector(locatorToBeFound));
	            } else {
	                options = currentModal.findElements(getSelector(locatorToBeFound));
	            }

	            for (WebElement option : options) {
	                if (option.isDisplayed() && option.isEnabled()) {
	                    ((JavascriptExecutor) driver).executeScript(
	                        "arguments[0].scrollIntoView({block: 'center', inline: 'nearest'});", option);
	                    actions.moveToElement(option).click().perform();
	                    Logger.info("✅ Dropdown option selected: " + locatorToBeFound);
	                    return;
	                }
	            }

	            // If no valid option found, press ARROW_DOWN to reveal more
	            dropdownTrigger.sendKeys(Keys.ARROW_DOWN);
	            Thread.sleep(300); // Allow UI to update after scrolling

	        } catch (Exception e) {
	            Logger.warn("⚠️ Exception while searching dropdown option: " + e.getMessage());
	        }

	        attempt++;
	    }

	    throw new Exception("❌ Option not found in dropdown: " + locatorToBeFound);
	}
	
	public void clickActiveElementByAttribute(String attribute ,String value) {
		WebElement activeElement = driver.switchTo().activeElement();
		if (value.equals(activeElement.getDomAttribute(attribute))) {
			activeElement.click();
		}
	}
	
	public void clickActiveElementByLocator(String locator) throws Exception {
		By selector = getSelector(locator);
		// get all elements
		List<WebElement> elements = driver.findElements(selector);
		//filter for visible
		 List<WebElement> visible = elements.stream()
				 .filter(WebElement::isDisplayed)
				 .collect(Collectors.toList());
		 if (visible.size() == 1) {
			 // on element
			 visible.get(0).click();
		 } else if (visible.isEmpty()) {
			 throw new Exception("No Visible Elements found");
		 } else {
			 throw new Exception("Too Many Elements found");
		 }
	}
	
	public void mouseClickActiveElementByLocator(String locator) throws Exception {
		By selector = getSelector(locator);
		// get all elements
		List<WebElement> elements = driver.findElements(selector);
		//filter for visible
		 List<WebElement> visible = elements.stream()
				 .filter(WebElement::isDisplayed)
				 .collect(Collectors.toList());
		 if (visible.size() == 1) {
			    WebElement screenObject = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(locator)));
				((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center', inline: 'nearest'});", visible.get(0));
				Actions actions = new Actions(driver);
				actions.moveToElement(visible.get(0)).click().perform();
		 } else if (visible.isEmpty()) {
			 throw new Exception("No Visible Elements found");
		 } else {
			 throw new Exception("Too Many Elements found");
		 }
	}
	
	public void clickActiveLocator(String locator) throws Exception {
		List<WebElement> buttons = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(getSelector(locator)));
		for (WebElement button : buttons){
			if (button.isEnabled()) {
				button.click();
				return;
			}
		}
		
		throw new Exception("No Active Button to click.");
	}

	public String getCaseID(String value) throws Exception {
		//check to see if value wrapped in {}
		String caseID= "";
	    if (value.contains("_")) {
	    	// split on underscore
	    	value = value.split("_")[0];
	    }
		if (value != null && value.matches("\\{[^{}*]+\\}")) {
			String recordHeader = value.substring(1, value.length()-1);
			if (ContextManager.containsKey(recordHeader)){
				caseID = (String) ContextManager.get(recordHeader);
			} else {
				caseID = recordHeader;
			}
		} else if (ContextManager.containsKey(value)){ 
			caseID = (String) ContextManager.get(value);
		} else if (value == null) {
			throw new Exception("Unable to get new case ID");
		}else {
			caseID = value.trim();
		}
		return caseID;
	}
	
	public void closeAllTabs(String locator) {
		// TODO Auto-generated method stub
		List<WebElement> closeButtons = driver.findElements(getSelector(locator));
		
		for (WebElement button : closeButtons) {
			if (button.isDisplayed() && button.isEnabled()) {
				button.click();
			}
		}
	}
	
	public void waitForPageToLoad() throws Exception {
		ExpectedCondition<Boolean> pageLoadCondition = driver ->
		((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete");
		
		try {
			wait.until(pageLoadCondition);
			Logger.info("Page Loaded");
		} catch (Exception e) {
			throw new Exception("Page Not loaded " + e);
		}
	}
	
	public void highlightElement(String locator) {
		WebElement element = driver.findElement(By.xpath(locator));
		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript("arguments[0].style.border='3px solid red'", element);
	}

	// ── Label-based form filling ─────────────────────────────────────────────

	/**
	 * Safely wraps a label string for use inside an XPath string predicate,
	 * handling labels that contain single quotes, double quotes, or both.
	 */
	private String xpathEscape(String text) {
		if (!text.contains("'"))  return "'" + text + "'";
		if (!text.contains("\"")) return "\"" + text + "\"";
		// Both quote types present — build an XPath concat() expression
		return "concat('" + text.replace("'", "', \"'\", '") + "')";
	}

	/**
	 * Finds the XPath locator for the form control associated with the given
	 * visible label text. Tries strategies in priority order — native inputs
	 * first, then custom dropdown widgets (role="combobox" / role="listbox").
	 *
	 * @throws RuntimeException if no matching field is found for the label
	 */
	private String findLocatorByLabel(String labelText) {
		String e = xpathEscape(labelText);
		String[] strategies = {
			// 1–3: Classic HTML for/id linkage — native controls
			"//input[@id = //label[normalize-space()=" + e + "]/@for]",
			"//textarea[@id = //label[normalize-space()=" + e + "]/@for]",
			"//select[@id = //label[normalize-space()=" + e + "]/@for]",
			// 4–6: ARIA label directly on a native control
			"//input[@aria-label=" + e + "]",
			"//textarea[@aria-label=" + e + "]",
			"//select[@aria-label=" + e + "]",
			// 7–9: Label element wraps the native control
			"//label[normalize-space()=" + e + "]//input",
			"//label[normalize-space()=" + e + "]//textarea",
			"//label[normalize-space()=" + e + "]//select",
			// 10–12: Immediately adjacent sibling — native controls
			"//label[normalize-space()=" + e + "]/following-sibling::input[1]",
			"//label[normalize-space()=" + e + "]/following-sibling::textarea[1]",
			"//label[normalize-space()=" + e + "]/following-sibling::select[1]",
			// 13–15: Broader following axis — native controls
			"//label[normalize-space()=" + e + "]/following::input[1]",
			"//label[normalize-space()=" + e + "]/following::textarea[1]",
			"//label[normalize-space()=" + e + "]/following::select[1]",
			// 16: Placeholder text fallback
			"//input[@placeholder=" + e + "]",
			// 17–19: Custom dropdown widgets — for/id and aria-label
			"//*[@role='combobox'][@id = //label[normalize-space()=" + e + "]/@for]",
			"//*[@role='combobox'][@aria-label=" + e + "]",
			"//*[@role='listbox'][@aria-label=" + e + "]",
			// 20–22: Label wraps or is sibling/following of a combobox widget
			"//label[normalize-space()=" + e + "]//*[@role='combobox']",
			"//label[normalize-space()=" + e + "]/following-sibling::*[@role='combobox'][1]",
			"//label[normalize-space()=" + e + "]/following::*[@role='combobox'][1]",
			// 23: aria-labelledby — element whose id is referenced by the label's for attribute
			"//*[@aria-labelledby = //label[normalize-space()=" + e + "]/@for][@role='combobox']",
		};
		for (String xpath : strategies) {
			if (elementExists(xpath)) {
				LOGGER.debug("Found field for label '{}' via: {}", labelText, xpath);
				return xpath;
			}
		}
		throw new RuntimeException("No form field found on page for label: '" + labelText + "'");
	}

	/**
	 * Detects the {@link FormFieldType} of the element at the given locator by
	 * inspecting its tag name, {@code type} attribute, and ARIA role.
	 *
	 * <ul>
	 *   <li>Native {@code <select>}            → DROPDOWN (Selenium Select)</li>
	 *   <li>Custom {@code role="combobox"}      → TYPEANDSELECT (type-ahead)</li>
	 *   <li>{@code <textarea>}                  → TEXTAREA</li>
	 *   <li>{@code input[type=checkbox]}         → CHECKBOX</li>
	 *   <li>{@code input[type=radio]}            → RADIO</li>
	 *   <li>Anything else                        → TEXTBOX</li>
	 * </ul>
	 */
	private FormFieldType detectFieldType(String locator) {
		WebElement element = waitForSelector(locator);
		String tag  = element.getTagName().toLowerCase();
		String role = element.getDomAttribute("role");

		if ("select".equals(tag))                return FormFieldType.DROPDOWN;
		if ("combobox".equals(role))             return FormFieldType.TYPEANDSELECT;
		if ("listbox".equals(role))              return FormFieldType.TYPEANDSELECT;
		if ("textarea".equals(tag))              return FormFieldType.TEXTAREA;
		String type = element.getDomProperty("type");
		if ("checkbox".equals(type))             return FormFieldType.CHECKBOX;
		if ("radio".equals(type))                return FormFieldType.RADIO;
		// input with aria-haspopup is a searchable custom dropdown
		String hasPopup = element.getDomAttribute("aria-haspopup");
		if (hasPopup != null && !hasPopup.equals("false")) return FormFieldType.TYPEANDSELECT;
		return FormFieldType.TEXTBOX;
	}

	/**
	 * Finds the form field associated with {@code labelText}, detects its type,
	 * and interacts with it using the appropriate BasePage method.
	 *
	 * <ul>
	 *   <li>Text / textarea           → {@link #enterTextInField}</li>
	 *   <li>Native dropdown (select)  → {@link #selectDropdownOptionByText}</li>
	 *   <li>Custom dropdown (combobox)→ {@link #typeAndSelectOption} (types value, picks from list)</li>
	 *   <li>Checkbox                  → {@link #setCheckboxCheckedState};
	 *       value must be {@code check/true/yes/1} or {@code uncheck/false/no/0}</li>
	 *   <li>Radio button              → {@link #selectRadioButton}</li>
	 * </ul>
	 *
	 * @throws RuntimeException if no field is found for the label, or if a
	 *                          checkbox receives an unrecognised value
	 */
	public void fillFieldByLabel(String labelText, String value) {
		String locator = findLocatorByLabel(labelText);
		FormFieldType type = detectFieldType(locator);
		String v = value.trim().toLowerCase();

		switch (type) {
			case CHECKBOX -> {
				boolean shouldCheck;
				if (v.equals("check") || v.equals("true") || v.equals("yes") || v.equals("1")) {
					shouldCheck = true;
				} else if (v.equals("uncheck") || v.equals("false") || v.equals("no") || v.equals("0")) {
					shouldCheck = false;
				} else {
					throw new RuntimeException(
						"Checkbox '" + labelText + "' requires check/uncheck/true/false/yes/no/1/0, got: '" + value + "'");
				}
				setCheckboxCheckedState(locator, shouldCheck);
			}
			case RADIO         -> selectRadioButton(locator);
			case DROPDOWN      -> selectDropdownOptionByText(locator, value);
			case TYPEANDSELECT -> typeAndSelectOption(locator, value);
			default            -> enterTextInField(locator, value);
		}
		LOGGER.info("Filled '{}' ({}) → '{}'", labelText, type, value);
	}
}



