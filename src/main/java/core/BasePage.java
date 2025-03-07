package core;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;
import java.util.List;

public class BasePage {
    protected WebDriver driver;
    private WebDriverWait wait;

    
    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10)); // Timeout from config
    }

    private By getLocator(String locator) {
        return locator.startsWith("//") ? By.xpath(locator) : By.cssSelector(locator);
    }

    private WebElement waitForElement(String locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(getLocator(locator)));
    }

    public void clickElementByLocator(String locator) {
        try {
            waitForElement(locator).click();
            System.out.println("✅ Clicked element: " + locator);
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to click element: " + locator, e);
        }
    }

    public void enterTextInField(String locator, String text) {
        try {
            WebElement element = waitForElement(locator);
            element.clear();
            element.sendKeys(text);
            System.out.println("✅ Entered text: " + text + " in " + locator);
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to enter text in: " + locator, e);
        }
    }

    public void selectDropdownOptionByText(String locator, String value) {
        try {
            Select dropdown = new Select(waitForElement(locator));
            dropdown.selectByVisibleText(value);
            System.out.println("✅ Selected dropdown value: " + value);
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
                System.out.println("✅ Checkbox state updated: Checked");
            } else if(!shouldBeChecked && isChecked) {
                System.out.println("ℹ️ Checkbox state updated: unchecked: ");
            } else {
            	 System.out.println("ℹ️ Checkbox in correct state");
            }
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to set checkbox state: " + locator, e);
        }
    }

    public void focusOnModal(String modalLocator) {
        try {
            WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(getLocator(modalLocator)));
            driver.switchTo().activeElement();
            System.out.println("✅ Switched to modal.");
        } catch (Exception e) {
            throw new RuntimeException("❌ Modal not found: " + modalLocator, e);
        }
    }

    public void closeModalWindow(String closeButtonLocator) {
        try {
            clickElementByLocator(closeButtonLocator);
            System.out.println("✅ Modal closed.");
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
            String screenshotPath = "screenshots/" + screenshotName + ".png";
            File destFile = new File(screenshotPath);
            srcFile.renameTo(destFile);
            System.out.println("✅ Screenshot taken: " + screenshotPath);
            return screenshotPath;
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to take screenshot: " + screenshotName, e);
        }
    }

    public void hoverOnElement(String locator) {
        try {
            Actions actions = new Actions(driver);
            actions.moveToElement(waitForElement(locator)).perform();
            System.out.println("✅ Hovered over element: " + locator);
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to hover over: " + locator, e);
        }
    }

    public void doubleClickElement(String locator) {
        try {
            Actions actions = new Actions(driver);
            actions.doubleClick(waitForElement(locator)).perform();
            System.out.println("✅ Double-clicked on: " + locator);
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to double-click: " + locator, e);
        }
    }

    public void rightClickElement(String locator) {
        try {
            Actions actions = new Actions(driver);
            actions.contextClick(waitForElement(locator)).perform();
            System.out.println("✅ Right-clicked on: " + locator);
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
                System.out.println("✅ Element is visible: " + locator);
            } else {
                wait.until(ExpectedConditions.invisibilityOfElementLocated(getLocator(locator)));
                System.out.println("✅ Element is not visible: " + locator);
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
            System.out.println("✅ Scrolled to element: " + locator);
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
            System.out.println("✅ Highlighted element: " + locator);
        } catch (Exception e) {
            System.out.println("⚠️ Failed to highlight element: " + locator);
        }
    }

    public String getTooltipFromElement(String locator) {
        try {
            WebElement element = waitForElement(locator);
            String tooltipText = element.getDomProperty("title");
            System.out.println("✅ Tooltip text: " + tooltipText);
            return tooltipText;
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to get tooltip from element: " + locator, e);
        }
    }
    
    public boolean elementExists(By locator) {
        try {
            return driver.findElement(locator).isDisplayed();
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
                    System.out.println("✅ Selected item: " + text);
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
                        System.out.println("✅ Option selected: " + option.getText());
                        found = true;
                        break;
                    }
                }

                if (found) break;
            }

            if (!found) {
                input.sendKeys(Keys.TAB);
                System.out.println("ℹ️ Option not found. Focus moved away from input.");
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
	        System.out.println("✅ File uploaded: " + filePath);
	    } catch (Exception e) {
	        throw new RuntimeException("❌ Failed to upload file: " + filePath, e);
	    }
	}
	
	public void uploadHiddenFile(String fileInputLocator, String filePath) {
	    try {
	        WebElement fileInput = driver.findElement(getLocator(fileInputLocator));
	        ((JavascriptExecutor) driver).executeScript("arguments[0].style.display = 'block';", fileInput);
	        fileInput.sendKeys(new File(filePath).getAbsolutePath());
	        System.out.println("✅ Hidden file input uploaded: " + filePath);
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

	        System.out.println("✅ Drag-and-drop file upload triggered: " + filePath);
	    } catch (Exception e) {
	        throw new RuntimeException("❌ Failed drag-and-drop upload for file: " + filePath, e);
	    }
	}
}


