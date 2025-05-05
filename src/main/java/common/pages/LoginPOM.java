package common.pages;

import core.BasePage;
import static org.junit.Assert.*;
import org.openqa.selenium.WebDriver;
import utils.jks.JKSReader;

public class LoginPOM extends BasePage {

    public final String usernameField = "//*[@id='userid']";  // CSS Selector
    public final String passwordField = "//*[@id='password']";  // CSS Selector
    public final String loginButton = "//button[contains(text(),'Sign In')]"; // Button selector
    public final String errorMessage = "//div[@id='error']"; // Error message selector
    public final String homepageTitle = "//*[@id='titleView!1Title']"; // Home Page Header

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
}

