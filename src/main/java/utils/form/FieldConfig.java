package utils.form;

public class FieldConfig {
    private final String fieldId;
    private final FormFieldType type;
    private final Runnable beforeFill;
    private final Runnable afterFill;

    public FieldConfig(String fieldId, FormFieldType type) {
        this(fieldId, type, null, null);
    }

    public FieldConfig(String fieldId, FormFieldType type, Runnable beforeFill, Runnable afterFill) {
        this.fieldId = fieldId;
        this.type = type;
        this.beforeFill = beforeFill;
        this.afterFill = afterFill;
    }

    public String getFieldId() {
        return fieldId;
    }

    public FormFieldType getType() {
        return type;
    }

    public Runnable getBeforeFill() {
        return beforeFill;
    }

    public Runnable getAfterFill() {
        return afterFill;
    }
}
