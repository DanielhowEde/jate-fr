package utils.form;

import core.BasePage;
import org.openqa.selenium.WebDriver;

import java.util.Map;

public class DynamicFormFiller extends BasePage {
    private final Map<String, FieldConfig> fieldMap;

    public DynamicFormFiller(WebDriver driver, Map<String, FieldConfig> fieldMap) {
        super(driver); // ✅ Pass to BasePage constructor
        this.fieldMap = fieldMap;
    }

    public void fill(Map<String, String> data) {
        for (Map.Entry<String, String> entry : data.entrySet()) {
            String fieldName = entry.getKey();
            String value = entry.getValue();

            if (value == null || value.equalsIgnoreCase("NULL")) continue;

            FieldConfig config = fieldMap.get(fieldName);
            if (config == null) {
                System.out.println("⚠️ No config found for field: " + fieldName);
                continue;
            }

            String locator = config.getFieldId();
            try {
                switch (config.getType()) {
                    case TEXTBOX:
                    case TEXTAREA:
                    case DATEPICKER:
                        enterTextInField(locator, value);
                        break;

                    case DROPDOWN:
                        selectDropdownOptionByText(locator, value);
                        break;

                    case CHECKBOX:
                        setCheckboxCheckedState(locator, Boolean.parseBoolean(value));
                        break;

                    case RADIO:
                        clickElementByLocator(locator); // Simplified – assumes locator is correct
                        break;

                    default:
                        System.out.println("⚠️ Unknown field type for: " + fieldName);
                }
            } catch (Exception e) {
                System.out.println("❌ Error processing field '" + fieldName + "': " + e.getMessage());
            }
        }
    }
}
