package common.pages;

import core.BasePage;
import static org.junit.Assert.*;

import org.jcodec.common.logging.Logger;
import org.openqa.selenium.WebDriver;

public class NavigationPOM extends BasePage {

	public static String btnHome = "//a[text()='Home']";
	public static String btnOpenCases = "//a[text()='Open Cases']";
	public static String btnClosedCases = "//a[text()='Closed Cases']";
	public static String btnChangeRequests= "//a[text()='Change Requests']";
	public static String btnKeyPerformaceInd = "//a[text()='Key Performance Indicators']";
	public static String btnSearch = "//button[@title='Expand search']";
	public static String lnkShowMore = "//a[@id='%s' and text()='Show More']";
	public static String lnkLocator = "//*[text()='%s']";
	
	
	public NavigationPOM(WebDriver driver) {
		super(driver);
		// TODO Auto-generated constructor stub
	}

/*	public void NavigateToTriage() {
		try {
			wait for 
		}
		if ()
	}*/

	public void navigateTo(String link) {
		
		switch (link.toLowerCase()) {
			case "home":
				clickElementBySelector(btnHome);
				break;
			case "closed cases":
				clickElementBySelector(btnClosedCases);
				break;
			case "open cases":
				clickElementBySelector(btnOpenCases);
				break;
			case "change requests":
				clickElementBySelector(btnChangeRequests);
				break;
			case "search":
				clickElementBySelector(btnSearch);
				break;
				
			case "key performance indicators":
				clickElementBySelector(btnKeyPerformaceInd);
				
				break;
			default:
				System.out.println("Unknown Function: "+ link);
				break;//click the show more group node and click on the element
			}
			
		captureAndAttachScreenshot("Navigated to " + link);
	}
		
	
	private void clickNavigationlnk(String text) {
		String formattedLink = String.format(lnkLocator, text);
		try {
			clickElementBySelector(formattedLink);
			
		}	catch (Exception e) {
			Logger.debug("Option Not Found: "+ text);
			formattedLink = String.format(lnkShowMore, text);
			clickElementBySelector(formattedLink);
		}
	}
	
}