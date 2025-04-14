package common.stepdefinitions;

import core.WebDriverManager;
import io.cucumber.java.en.*;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.WebDriver;
import common.pages.LoginPOM;
import common.pages.HomePagePOM;
import static org.junit.Assert.*;

public class NavigationSteps {

	private WebDriver driver;
	
	@When("^I open the (?:url|site) \"([^\"]*)\"$")
	public void openSite(String url) {
	    driver.get(url);
	}
}
