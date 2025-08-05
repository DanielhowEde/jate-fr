package common.pages;

import core.BasePage;
import utils.context.*;
import static org.junit.Assert.*;

import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WindowType;

import utils.jks.JKSReader;

public class LoginPOM extends BasePage {

    public final String usernameField = "//input[@placeholder='Username']";  // CSS Selector
    public final String passwordField = "//input[@placeholder='Password']";  // CSS Selector
    public final String loginButton = "//button[contains(@class,'loginButton')]"; // Button selector
    public final String errorMessage = "//div[@id='error']"; // Error message selector
    public final String homepageTitle = "//*[@id='titleView!1Title']"; // Home Page Header

    public String optUserProfile="//*[@class='comm-user-profile-menu']";
    public String optLogOut = "//span[@title='Log Out']/ancestor::a";
    
    // Outlook buttons
    public final String outlookUserName = "";
    public final String outlookPassword = "";
    public final String outlookLoginButton = "";
    
    // site fields
    public final String siteUserName = "//input[@id='username']";
    public final String sitePassword = "//input[@id='password']";
    public final String siteLoginButton = "//input[@id='Login']";

    public LoginPOM (WebDriver driver) {
        super(driver);
    }

    public void enterUsername(String username) {
        enterTextInField(usernameField, username);
    }

    public void enterPassword(String password) {
        enterTextInField(passwordField, password);
    }

    public void clickLoginButton() {
        clickElementBySelector(loginButton);
    }
    
    public void loginAsUser(String userType) {
    	JKSReader reader = new JKSReader();
    	String[] creds = reader.getSecret(userType).split(",");
    	String username = creds[0];
    	String password = creds[1];
    	enterTextInField(usernameField, username.trim());
    	enterTextInField(passwordField, password.trim());
    	clickLoginButton();
    }
    

	public boolean confirmLogin() {
		// check for error message
		if (isElementVisible(errorMessage) == true) { ;
			String actual = getTextFromElement(errorMessage);
			String expected = "Authentication failed.";
			assertTrue("Text is not expected",actual.contains(expected));
			return false;
		} else {
			String actual = getTextFromElement(homepageTitle);
			String expected = "Program Points Balance Sheet";
			assertTrue("Home Page Title not present",actual.contains(expected));
			return true;
		}
	}

	public void LoginToOutlook(String userType) {
	    	JKSReader reader = new JKSReader();
    		String[] creds = reader.getSecret(userType).split(",");
    		String username = creds[0];
    		String password = creds[1];
    		enterTextInField(outlookUserName, username.trim());
    		enterTextInField(outlookPassword, password.trim());
    		clickElementBySelector(outlookLoginButton);
		
	}

	public void logOut() throws Exception {
		selectMenuOption(optUserProfile, optLogOut);
		
	}

	public void loginToSite(String user) throws Exception {
		String HomeTab = driver.getWindowHandle();
		ContextManager.put("HomeTab", HomeTab);
		if (isElementVisible(siteUserName)) {
	
	    	JKSReader reader = new JKSReader();
			String[] creds = reader.getSecret(user).split(",");
			String username = creds[0];
			String password = creds[1];
			enterTextInField(siteUserName, username.trim());
			enterTextInField(sitePassword, password.trim());
			clickElementBySelector(siteLoginButton);
		}
	}
}

