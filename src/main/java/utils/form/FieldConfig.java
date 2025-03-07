package utils.form;

import utils.form.FormFieldType;

public class FieldConfig {
    private final String fieldId;         // HTML ID or locator key
    private final FormFieldType type;     // TEXTBOX, DROPDOWN, etc.

    public FieldConfig(String fieldId, FormFieldType type) {
        this.fieldId = fieldId;
        this.type = type;
    }

    public String getFieldId() {
        return fieldId;
    }

    public FormFieldType getType() {
        return type;
    }
}