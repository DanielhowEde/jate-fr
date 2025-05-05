package utils.form;

import core.BasePage;
import org.openqa.selenium.WebDriver;
import java.util.LinkedHashMap;
import java.util.Map;
import utils.form.FieldConfig;

public class DynamicFormFiller extends BasePage {
    private final LinkedHashMap<String, FieldConfig> fieldMap;

    public DynamicFormFiller(WebDriver driver, Map<String, FieldConfig> fieldMapping) {
        super(driver);
        this.fieldMap = (fieldMapping != null) ? new LinkedHashMap<>(fieldMapping) : new LinkedHashMap<>();
    }

    public void fill(Map<String, String> data) {
        if (fieldMap.isEmpty()) {
            System.out.println("⚠️ No field mapping provided. Skipping form fill.");
            return;
        }

        for (Map.Entry<String, FieldConfig> entry : fieldMap.entrySet()) {
            String fieldName = entry.getKey();
            FieldConfig config = entry.getValue();
            String value = data.get(fieldName);

            if (value == null || value.equalsIgnoreCase("NULL")) {
                System.out.println("⚠️ Skipping field '" + fieldName + "' due to null or 'NULL' value.");
                continue;
            }

            String locator = config.getFieldId();
            Runnable before = config.getBeforeFill();
            Runnable after = config.getAfterFill();

            try {
                if (before != null) before.run();

                switch (config.getType()) {
                    case TEXTBOX:
                    case TEXTAREA:
                        enterTextInField(locator, value);
                        break;
                    case TYPEANDSELECT:
                    	this.typeAndSelectOption(locator, value);
                    	break;
                    case DATEPICKER:
                        selectDateFromCalendar(locator, value);
                        break;
                    case DROPDOWN:
                        selectDropdownOptionByText(locator, value);
                        break;
                    case CHECKBOX:
                        setCheckboxCheckedState(locator, Boolean.parseBoolean(value));
                        break;
                    case RADIO:
                    	selectRadioButton(locator);
                        break;
                    default:
                        System.out.println("⚠️ Unknown field type for: " + fieldName);
                }

                if (after != null) after.run();
            } catch (Exception e) {
                System.out.println("❌ Error processing field '" + fieldName + "': " + e.getMessage());
            }
        }
    }
}
