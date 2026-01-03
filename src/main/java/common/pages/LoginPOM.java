package common.pages;

import core.BasePage;
import utils.context.*;
import java.util.Set;

import org.openqa.selenium.WebDriver;
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
    	closeAllTabs();
    	
    	JKSReader reader = new JKSReader();
    	String[] creds = reader.getSecret(userType).split(",");
    	String username = creds[0];
    	String password = creds[1];
    	enterTextInField(usernameField, username.trim());
    	enterTextInField(passwordField, password.trim());
    	clickLoginButton();
    }
    

	
	private void closeAllTabs() {
		String HomeTab = driver.getWindowHandle();
		Set<String> allTabs = driver.getWindowHandles();
		for (String tab : allTabs) {
			if (!tab.equals(HomeTab)) {
				driver.switchTo().window(tab);
				driver.close();
			}
		}
		driver.switchTo().window(HomeTab);
	}

	public void LoginToOutlook(String userType) {
		closeAllTabs();
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
		waitForPageToLoad();
	}

	public void loginToSite(String user) throws Exception {
		String HomeTab = driver.getWindowHandle();
		ContextManager.put("HomeTab", HomeTab);
		
		closeAllTabs();
		
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

