package common.pages;

import core.BasePage;
import static org.junit.Assert.*;

import org.jcodec.common.logging.Logger;
import org.openqa.selenium.WebDriver;

public class NavigationPOM extends BasePage {

	public static String btnHome = "//a[@title='Home']";
	public static String lnkShowMore = "//a[@id='%s' and text()='Show More']";
	public static String lnkLocator = "//*[text()='%s']";
	
	
	public NavigationPOM(WebDriver driver) {
		super(driver);
		// TODO Auto-generated constructor stub
	}



	public void navigateTo(String link) {
		
		String[] navigationSegment = link.trim().split(",");
		String btnLocator;
		String groupNode;
		int i = 0;
		for (String segment : navigationSegment) {
			i++;
			switch (segment.trim()) {
				case "Home":
					clickElementBySelector(btnHome);
					break;
				case "My Team":
					clickNavigationlnk(segment);
					break;
				case "Me":
					clickNavigationlnk(segment);
					break;
				case "Procurement":
					clickNavigationlnk(segment);
					break;
				case "My Client Groups":
					groupNode="showmore_groupNode_workforce_management";
					clickNavigationlnk(segment);
					break;
				case "Help Desk":
					clickNavigationlnk(segment);
					break;
				case "Product Management":
					clickNavigationlnk(segment);
					break;
				case "Benefits Administration":
					clickNavigationlnk(segment);
					break;
				case "Field Service":
					clickNavigationlnk(segment);
					break;
				case "Knowledge":
					clickNavigationlnk(segment);
					break;
				case "Supply Chain Planning":
					clickNavigationlnk(segment);
					break;
					
				default:
					try {
						scrollToIfPresent(segment);
						clickNavigationlnk(segment);
					} catch (Exception e){
						//click the show more group node and click on the element
				}
				captureAndAttachScreenshot("Navigation Step " + i);
			}
		}
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