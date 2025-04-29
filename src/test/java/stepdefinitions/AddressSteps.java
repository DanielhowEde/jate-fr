package stepdefinitions;

import org.openqa.selenium.WebDriver;

import core.WebDriverManager;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import pages.AddressesPOM;

public class AddressSteps {
	
    WebDriver driver = WebDriverManager.getDriver("chrome");
    AddressesPOM addressesPage = new AddressesPOM(driver);

    @Then("I enter all address fields for {string}")
	public void i_enter_all_address_fields(String iteration) {
    	int rowIndex = Integer.parseInt(iteration.trim()) - 1; // Excel-like indexing (1-based)
		addressesPage.fillAddressFormFromSpreadsheetRow(rowIndex);
    }
    
    @When("Click the Go Button")
    public void click_the_go_button() {
    	addressesPage.clickGoButton();

    }
    
    @Then("I see the results as outline in the spreadsheet for line {string}")
    public void i_see_the_results_as_outline_in_the_spreadsheet(String iteration) {
        // Write code here that turns the phrase above into concrete actions
    	int rowIndex = Integer.parseInt(iteration.trim()) - 1;
    	addressesPage.verifyAddressBubbleVisible(rowIndex);
    }

}
