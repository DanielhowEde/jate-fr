package common.pages;

import core.BasePage;
import static org.junit.Assert.*;
import org.openqa.selenium.WebDriver;

public class NavigationPOM extends BasePage {

	public final static String btnHome = "//button[text()='home']";
	public final static String lnkShowMore = "//button[text()='show more']";
	
	
	public NavigationPOM(WebDriver driver) {
		super(driver);
		// TODO Auto-generated constructor stub
	}



	public void navigateTo(String link) {
		
		String[] navigationSegment = link.trim().split(",");
		
		for (String segment : navigationSegment) {
			
			switch (segment.trim()) {
				case "Home":
					clickElementByLocator(btnHome);
					break;
				case "Option":
					//do something
				default:
					String btnLocator = "//button[text()='" + segment.trim() + "']";
					try {
						clickElementByLocator(btnLocator);
						
					}	catch (Exception e) {
						clickElementByLocator(lnkShowMore);
						clickElementByLocator(btnLocator);
					}
			}
		}
	}
	
}