package common.pages;

import core.BasePage;
import static org.junit.Assert.*;
import org.openqa.selenium.WebDriver;
import utils.jks.JKSReader;
import utils.secrets.SecretManagerUtil;

public class LoginPOM extends BasePage {

    public final String usernameField = "//*[@id='Username']";  // CSS Selector
    public final String passwordField = "//*[@id='Password']";  // CSS Selector
    public final String loginButton = "//button[contains(text(),'Login')]"; // Button selector
    public final String errorMessage = "//*[starts-with(@style,'color: red')]"; // Error message selector
    public final String homepageTitle = "//h1[contains(text(), 'Test Elements')]"; // Home Page Header

    public LoginPOM(WebDriver driver) {
        super(driver);
    }

    public void enterUsername(String username) {
        enterTextInField(usernameField, username);
    }

    public void enterPassword(String password) {
        enterTextInField(passwordField, password);
    }

    public void clickLoginButton() {
        clickElementByLocator(loginButton);
    }
    
    public void loginAsUser(String userType) {
    	JKSReader reader = new JKSReader();
    	String[] creds = reader.getSecret(userType).split(",");
    	String username = creds[0];
    	String password = creds[1];
    	
    	enterTextInField(usernameField, username);
    	enterTextInField(passwordField, password);
    	clickLoginButton();
        
    }
    

	public boolean confirmLogin() {
		// check for error message
		if (isElementVisible(errorMessage) == true) { ;
			String actual = getTextFromElement(errorMessage);
			String expected = "Invalid username or password";
			assertTrue("Text is not expected",actual.contains(expected));
			return false;
		} else {
			String actual = getTextFromElement(homepageTitle);
			String expected = "Test Elements";
			assertTrue("Home Page Title not present",actual.contains(expected));
			return true;
		}
	}

}

