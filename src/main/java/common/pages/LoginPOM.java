package common.pages;

import core.BasePage;
import utils.context.*;
import java.util.Set;

import org.openqa.selenium.WebDriver;
import utils.jks.JKSReader;

public class LoginPOM extends BasePage {

    public final String usernameField = "//input[@placeholder='Username']";
    public final String passwordField = "//input[@placeholder='Password']";
    public final String loginButton = "//button[contains(@class,'loginButton')]";
    public final String errorMessage = "//div[@id='error']";
    public final String homepageTitle = "//*[@id='titleView!1Title']";

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

    /**
     * Parses a "user,password" secret string from the JKS into a two-element array.
     * Throws a descriptive exception if the format is wrong.
     */
    private String[] parseCredentials(String alias) {
        JKSReader reader = new JKSReader();
        String secret = reader.getSecret(alias);
        String[] parts = secret.split(",", 2);
        if (parts.length < 2 || parts[0].isBlank() || parts[1].isBlank()) {
            throw new RuntimeException(
                "JKS secret for alias '" + alias + "' must be in 'username,password' format");
        }
        return parts;
    }

    public void loginAsUser(String userType) {
    	closeAllTabs();
    	String[] creds = parseCredentials(userType);
    	enterTextInField(usernameField, creds[0].trim());
    	enterTextInField(passwordField, creds[1].trim());
    	clickLoginButton();
    }

	private void closeAllTabs() {
		String homeTab = driver.getWindowHandle();
		Set<String> allTabs = driver.getWindowHandles();
		for (String tab : allTabs) {
			if (!tab.equals(homeTab)) {
				driver.switchTo().window(tab);
				driver.close();
			}
		}
		driver.switchTo().window(homeTab);
	}

	public void LoginToOutlook(String userType) {
		closeAllTabs();
		String[] creds = parseCredentials(userType);
		enterTextInField(outlookUserName, creds[0].trim());
		enterTextInField(outlookPassword, creds[1].trim());
		clickElementBySelector(outlookLoginButton);
	}

	public void logOut() throws Exception {
		selectMenuOption(optUserProfile, optLogOut);
		waitForPageToLoad();
	}

	public void loginToSite(String user) throws Exception {
		String homeTab = driver.getWindowHandle();
		ContextManager.put("HomeTab", homeTab);
		closeAllTabs();
		if (isElementVisible(siteUserName)) {
			String[] creds = parseCredentials(user);
			enterTextInField(siteUserName, creds[0].trim());
			enterTextInField(sitePassword, creds[1].trim());
			clickElementBySelector(siteLoginButton);
		}
	}
}
