package common.stepdefinitions;

import core.WebDriverManager;
import io.cucumber.java.en.*;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.WebDriver;
import common.pages.LoginPOM;
import static org.junit.Assert.*;

public class loginSteps {
    WebDriver driver = WebDriverManager.getDriver("chrome");
    LoginPOM loginPage = new LoginPOM(driver);

  	
    @Given("I open the login page")
    public void navigateToLoginPage() {
        driver.get("http://localhost:3000/");
    }
    
    @Given("I login as {string} user")
    public void loginAsUser(String user){
    	loginPage.loginAsUser(user);
    }

    @When("I enter {string} into the username field")
    public void enterUsername(String username) {
        loginPage.enterUsername(username);
    }

    @When("I enter {string} into the password field")
    public void enterPassword(String password) {
        loginPage.enterPassword(password);
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
}

