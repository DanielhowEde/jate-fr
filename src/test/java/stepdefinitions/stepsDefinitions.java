package stepdefinitions;

import org.openqa.selenium.WebDriver;

import core.WebDriverManager;
import io.cucumber.java.en.Given;

import pages.RadioButtonsPOM;

public class stepsDefinitions {
	
    WebDriver driver = WebDriverManager.getDriver("chrome");
    RadioButtonsPOM radioButtonPage = new RadioButtonsPOM(driver);


    @Given("I click on the radio button for {string}")
    public void clickOnRaidoButton(String radioButton) {
    	radioButtonPage.clickRadioButton(radioButton);
        
    }
    

    @Given("I select the radio button for {string}")
    public void SelectRaidoButton(String radioButton) {
    	radioButtonPage.selectRadioButton(radioButton);
        
    }
}
