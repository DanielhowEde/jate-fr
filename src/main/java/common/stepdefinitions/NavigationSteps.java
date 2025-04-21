package common.stepdefinitions;


import io.cucumber.java.en.*;
import org.openqa.selenium.WebDriver;



public class NavigationSteps {

	private WebDriver driver;
	
	@When("^I open the (?:url|site) \"([^\"]*)\"$")
	public void openSite(String url) {
	    driver.get(url);
	}
}
