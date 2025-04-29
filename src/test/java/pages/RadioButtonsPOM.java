package pages;

import core.BasePage;

import org.openqa.selenium.WebDriver;


public class RadioButtonsPOM extends BasePage{
	
	public final String radioButton = "//input[@value='%s']";  // CSS Selector
    public final String Option2 = "//*[@value='Option 2']";  // CSS Selector
    public final String loginButton = "//button[contains(text(),'Login')]"; // Button selector
    public final String errorMessage = "//*[starts-with(@style,'color: red')]"; // Error message selector
    public final String homepageTitle = "//h1"; // Home Page Header

	public RadioButtonsPOM(WebDriver driver) {
		super(driver);
		// TODO Auto-generated constructor stub
	}

	public void clickRadioButton(String radioButtonOption) {
		String locator = String.format(radioButton, radioButtonOption);
		clickElementByLocator(locator);
		assertRadioButtonSelected(locator, true); // assert radio selected
	}

	public void selectRadioButton(String radioButtonOption) {
		String locator = String.format(radioButton, radioButtonOption);
		assertRadioButtonSelected(locator, false); // assert radio button is not selected
		clickElementByLocator(locator);
		assertRadioButtonSelected(locator, true); // assert radio selected
	}
	
	
}