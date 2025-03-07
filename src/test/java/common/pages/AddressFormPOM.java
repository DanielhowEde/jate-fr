package common.pages;

import core.BasePage;
import static org.junit.Assert.*;
import org.openqa.selenium.WebDriver;
import utils.form.FieldConfig;
import utils.form.FormFieldType;
import java.util.Map;
import java.util.HashMap; // Optional if using mutable map

import utils.form.DynamicFormFiller;
import utils.jks.JKSReader;

public class AddressFormPOM {
    WebDriver driver;
    DynamicFormFiller formFiller;

    public AddressFormPOM(WebDriver driver) {
        this.driver = driver;
        this.formFiller = new DynamicFormFiller(driver, getFieldMap());
    }

    private Map<String, FieldConfig> getFieldMap() {
        return Map.of(
            "AddressLine1", new FieldConfig("address1", FormFieldType.TEXTBOX),
            "AddressLine2", new FieldConfig("address2", FormFieldType.TEXTBOX),
            "County", new FieldConfig("county", FormFieldType.DROPDOWN),
            "AgreeToTerms", new FieldConfig("agree", FormFieldType.CHECKBOX)
        );
    }

    public void fillFields(Map<String, String> data) {
        formFiller.fill(data);
    }
}