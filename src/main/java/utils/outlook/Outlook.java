package utils.outlook;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.devtools.idealized.Javascript;
import org.openqa.selenium.devtools.v131.headlessexperimental.model.ScreenshotParams.Format;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.TimeoutException;
import java.time.Duration;
import java.util.NoSuchElementException;

import core.WebDriverManager;
import core.BasePage;
import core.Hooks;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Outlook extends BasePage{

	public static String inboxLocator = "//*[@role='treeitem' and @data-folder-name='inbox']";
	public static String newEmailButton = "//button[@aria-label='New email']";
	public static String toField = "//*[@aria-label='To']";
	public static String subjectField = "//*[@aria-label='Subject']";
	public static String messageBody = "//*[@aria-label='Message body, press Alt+F10 to exit']";
	public static String sendButton = "//button[@aria-label='Send']";
	public static String filterButton = "//button[@aria-label='Filter']";
	public static String attachButton = "//*[@class='ribbon-menu-text' and contains(text(),'Attach')]";
	public static String oneDrive = "//*[text()='OneDrive']";
	public String filterOption = "//*[@title='%s']";
	public String oneDriveFile = "//*[contains(@title,'%s')]";
	public String shareLinkButton = "//*[text()='Share link']";
			
	
    public Outlook(WebDriver driver) {
		super(driver);
		
	}


    public void openOutlookSharedFolder(String sharedFolder) throws Exception {
        // Open a new tab
        ((JavascriptExecutor) driver).executeScript("window.open()");

        // Navigate to your inbox then select shared folder inbox
        String outlookUrl = "https://outlook.office.com/mail/" + sharedFolder + "/";
        driver.get(outlookUrl);
        try {
        	waitForSelector(inboxLocator);
        } catch (Exception e) {
        	// shared folder not expanded
        	String sharedFolderLoc ="//span[text()='"+sharedFolder+"']";
        	clickOptions(sharedFolderLoc);
        	waitForSelector(inboxLocator);
        }
        clickElementBySelector(inboxLocator);
        
    }
    
    public void createEmailInOutlook(String destinationAddress, String subject, String emailText, String attachment) throws Exception {

	    	waitForSelector(newEmailButton);
	    	clickElementBySelector(newEmailButton);
	    	waitForSelector(toField);
	    	Thread.sleep(500);
	    	clickElementBySelector(toField);
	    	
	    	driver.switchTo().activeElement().sendKeys(destinationAddress,Keys.ENTER);
	    	
	    	String toAddressPopUp = "//span[text()='"+destinationAddress.trim() + "']";
	    	
	    	Thread.sleep(500);
	    	if (elementExists(toAddressPopUp)) {
	    		clickElementBySelector(toAddressPopUp);
	    	}
	    	
	    	enterTextInField(subjectField,subject);
	    	
	    	// Enter Text from text file in the testdata directory
	    	String emailBody = Files.readString(Paths.get("testdata/" + emailText + ".txt"));
	    	clickElementBySelector(messageBody);
	    	enterTextInField(messageBody,emailBody);
	    	
	    	// add attachment to email 
	    	if (!attachment.equals("no")) {
	    		clickElementBySelector(attachButton);
	    		waitForSelector(oneDrive);
	    		clickElementBySelector(oneDrive);
	    		
	    		// wait for file explore to open need to do some clever stuff here as this is outside the browser
	    		Thread.sleep(2000);
	    		clickElementBySelector(String.format(oneDriveFile, attachment));
	    		Thread.sleep(500);
	    		clickElementBySelector(shareLinkButton);
	    		
	    		//wait for 2 seconds for attachment to be linked
	    		Thread.sleep(2000);
	    	}
	    	
	    	// Wait for a second before sending email
	    	captureScreenshot("Sending Email");
	    	Thread.sleep(1000);
	    	clickElementBySelector(sendButton);
	    	System.out.println("Email Sent");
	    
    }
    
    private void uploadFileKeyboardCommands(String attachment) throws Exception {
		Robot robot = new Robot();
		StringSelection selection= new StringSelection(attachment);
		
		robot.keyPress(KeyEvent.VK_CONTROL);
		robot.keyPress(KeyEvent.VK_V);
		
		robot.keyRelease(KeyEvent.VK_V);
		robot.keyRelease(KeyEvent.VK_CONTROL);
		
		// press Enter
		robot.keyPress(KeyEvent.VK_ENTER);
		robot.keyRelease(KeyEvent.VK_ENTER);
		
		
	}


	public void FilterBy(String subOption) throws Exception {
    	clickElementBySelector(filterButton);
    	String thisFilterOption = String.format(filterOption, subOption);
    	clickElementBySelector(thisFilterOption);
    }
    
    
	public boolean waitForNewEmail(int timeoutSeconds) throws Exception {
	    String locator = "//*[@aria-label[starts-with(.,'Unread ')]]";
	    Thread.sleep(20000);
	    try {
	    	WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
	        
	        // Wait for the element to be visible within the specified timeout
	        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(locator)));
	        
	        // If the element is found and visible, return true
	        return element != null && element.isDisplayed();
	    } catch (TimeoutException e) {
	        // Timeout reached, no new email found
	        return false;
	    }
	}
    
    public static void markEmailAsRead(boolean status, WebDriver driver, WebElement webElement) {
    	Actions action = new Actions(driver);
    	String option;
    	if (status==true) {
    		option="//span[text()='Mark as read']";
    	} else {
    		option="//span[text()='Mark as unread']";
    	}
    	
    	action.contextClick(webElement).perform();

    	WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        WebElement markAsReadOption = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(option))); 
        markAsReadOption.click();
    }


    public void markAllEmailsAsRead() {
        // XPath selectors
        String unreadMessages = "//*[contains(@aria-label,'Unread ')]";
        String markAsRead = "//span[text()='Mark as read']";
        
        while (elementExists(unreadMessages)) {
        	try {
        		List<WebElement> unreadEmails = driver.findElements(By.xpath(unreadMessages));
        		if (unreadEmails.isEmpty()) {
        			System.out.println("No more unread emails");
        			break;
        		}
        		WebElement email = unreadEmails.get(0);
        		
        		
        		Actions actions = new Actions(driver);
        		actions.contextClick(email).perform();
        		
        		// wait for 'Mark as read' option then click it
        		WebElement markAsReadOption = new WebDriverWait(driver, Duration.ofSeconds(5)).until(ExpectedConditions.elementToBeClickable(By.xpath(markAsRead)));
        		markAsReadOption.click();
        		Thread.sleep(500);
        	} catch (Exception e) {
        		System.out.println(" Failed to mark email as read: "+ e.getMessage());
        	}
        }
  /*      if (elementExists(unreadMessages)) {
            List<WebElement> unreadEmails = driver.findElements(By.xpath(unreadMessages));
            for (WebElement email : unreadEmails) {
            	try {
            		Actions actions = new Actions(driver);
            		actions.contextClick(email).perform();
            		
            		// wait for 'Mark as read' option then click it
            		WebElement markAsReadOption = new WebDriverWait(driver, Duration.ofSeconds(5)).until(ExpectedConditions.elementToBeClickable(By.xpath(markAsRead)));
            		markAsReadOption.click();
            		Thread.sleep(500);
            	} catch (Exception e) {
            		System.out.println(" Failed to mark email as read: "+ e.getMessage());
            	}
            }
        }*/
    }
}