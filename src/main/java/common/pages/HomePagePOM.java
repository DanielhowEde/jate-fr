package common.pages;

import core.BasePage;

import org.openqa.selenium.WebDriver;


public class HomePagePOM extends BasePage{
    public HomePagePOM(WebDriver driver) {
		super(driver);
	}
    
    public static String lnkHeader = "//*[text()='%s']";
    public static String pageTitle = "//h1[contains(text(), '%s')]";
    public final static String pageHeader = "//h1";
    public final static String pageName = "%s Test";
    

	public void navigateToLink(String lnkName) {
		clickElementByLocator(String.format(lnkHeader, lnkName));
	    String thisPage = getTextFromElement(pageHeader);
	    String expectedPageName = String.format(pageName, lnkName);
	    assertValue(pageName, "Header", expectedPageName , thisPage);
	}        
}