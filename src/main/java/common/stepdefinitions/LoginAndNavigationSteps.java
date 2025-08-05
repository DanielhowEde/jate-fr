package common.stepdefinitions;

import core.WebDriverManager;
import io.cucumber.java.en.*;
import org.openqa.selenium.WebDriver;
import common.pages.LoginPOM;
import common.pages.NavigationPOM;
import static org.junit.Assert.*;

public class LoginAndNavigationSteps {
	WebDriver driver = WebDriverManager.getDriver(); 
    LoginPOM loginPage = new LoginPOM(driver);
    NavigationPOM navigationPage = new NavigationPOM(driver);
  	
	@Given("I open the URL {string}")
	public void openSite(String url) {
	    driver.get(url);
	}
	
	@Given("I enter {string}  into the username field")
    public void enterUsername(String user){
    	loginPage.enterUsername(user);
    }
	
	@Given("I login to site as {string} user")
	public void loginToTriage(String user) throws Exception {
		loginPage.loginToSite(user);
	}
    
    @Given("I enter {string} into the password field")
    public void enterPassword(String user){
    	loginPage.enterPassword(user);
    }
       
    @Given("I login as {string} user")
    public void loginAsUser(String user){
    	loginPage.loginAsUser(user);
    }

    @When("I click the login button")
    public void clickLoginButton() {
        loginPage.clickLoginButton();
    }
    
    @When("I should be redirected to the home page")
    public void congfirmLogin() {
    	boolean actualStatus = loginPage.confirmLogin();
        assertTrue("Home page not loaded", actualStatus);
    }
    
    @When("I should see an error message")
    public void checkForError() {
    	boolean actualStatus = loginPage.confirmLogin();
        assertFalse("Home page not loaded", actualStatus);
    }
    
    @Given("Login to Outlook as {string}")
    public void LoginToOutlook(String userType) {
    	loginPage.LoginToOutlook(userType);
    }
    
    
    @Given("I navigate to {string}")
    public void navigateToPage(String link) {
    	navigationPage.navigateTo(link);
    }
	
    @Then("the user logs out")
    public void logoutStep() throws Exception {
	loginPage.logOut();
    }    

}

