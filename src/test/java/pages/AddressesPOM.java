package pages;

import core.BasePage;
import core.Hooks;
import org.openqa.selenium.WebDriver;
import utils.context.ScenarioContext;
import utils.excel.ExcelFileManager;
import utils.form.DynamicFormFiller;
import utils.form.FieldConfig;
import utils.form.FormFieldType;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class AddressesPOM extends BasePage {

    // ───── Locators ─────
    private final String txtName = "//*[@id='name']";
    private final String txtStreet = "//*[@id='street']";
    private final String txtCity = "//*[@id='city']";
    private final String drdState = "//*[@id='state']";
    private final String txtZIP = "//*[@id='zip']";
    private final String radUS = "//input[@type='radio' and @value='us']";
    private final String radUK = "//input[@type='radio' and @value='uk']";
    private final String btnGo = "//button[text()='Go']";

    // ───── Dependencies ─────
    private final WebDriver driver;
    private final DynamicFormFiller formFiller;
    private final ScenarioContext context = Hooks.getScenarioContext();

    // ───── Constructor ─────
    public AddressesPOM(WebDriver driver) {
        super(driver);
        this.driver = driver;
        this.formFiller = new DynamicFormFiller(driver, getFieldMapping());
    }

    // ───── Public Actions ─────
    public void fillAddressFormFromSpreadsheetRow(int index) {
        Map<String, String> row = getSpreadsheetRow(index);
        System.out.println("▶️ Filling address form from row " + index + ": " + row);
        selectCountryRadio(row.get("country"));
        formFiller.fill(row);
    }

    public void clickGoButton() {
        clickElementByLocator(btnGo);
    }

    public void verifyAddressBubbleVisible(int index) {
        Map<String, String> row = getSpreadsheetRow(index);
        String addressLocator = buildBubbleLocator(row);
        waitUntilElementVisible(addressLocator);
        assertTrue("❌ Expected address is not visible in the bubble", isElementVisible(addressLocator));
    }

    // ───── Private Helpers ─────
    private Map<String, FieldConfig> getFieldMapping() {
        return Map.of(
            "name", new FieldConfig(txtName, FormFieldType.TEXTBOX),
            "street", new FieldConfig(txtStreet, FormFieldType.TEXTBOX),
            "city", new FieldConfig(txtCity, FormFieldType.TEXTBOX),
            "state", new FieldConfig(drdState, FormFieldType.DROPDOWN),
            "zip", new FieldConfig(txtZIP, FormFieldType.TEXTBOX)
        );
    }

    private Map<String, String> getSpreadsheetRow(int index) {
        String spreadsheetPath = (String) context.get("spreadsheet");
        ExcelFileManager excel = new ExcelFileManager(spreadsheetPath, null);
        List<Map<String, String>> rows = excel.getAllRows();

        if (index < 0 || index >= rows.size()) {
            throw new RuntimeException("❌ Invalid index " + index + ". Spreadsheet only contains " + rows.size() + " rows.");
        }

        return rows.get(index);
    }

    private void selectCountryRadio(String country) {
        if ("uk".equalsIgnoreCase(country)) {
            clickElementByLocator(radUK);
        } else {
            clickElementByLocator(radUS);
        }
    }

    private String buildBubbleLocator(Map<String, String> row) {
        return String.format(
            "//div[contains(., '%s') and contains(., '%s') and contains(., '%s') and contains(., '%s') and contains(., '%s')]",
            row.getOrDefault("name", "").trim(),
            row.getOrDefault("street", "").trim(),
            row.getOrDefault("city", "").trim(),
            row.getOrDefault("zip", "").trim(),
            row.getOrDefault("state", "").trim()
        );
    }
}
