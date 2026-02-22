package utils.stories.yaml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Describes how to resolve Excel data for a single run item.
 *
 * <p>Exactly one of {@code row}, {@code key}, or {@code rows} should be set.
 * If none is set, the first data row of the sheet is used.
 *
 * <pre>
 * data:
 *   sheet: Users
 *   row: 1             # 1-based row index (row 1 = first data row after header)
 *
 * data:
 *   sheet: Users
 *   key:
 *     column: testCaseId
 *     value: "TC-101"
 *
 * data:
 *   sheet: Users
 *   rows: all           # injects all rows as List under StoryContext key "rows"
 * </pre>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class YamlDataMapping {

    private String sheet;
    private Integer row;
    private String rows;
    private KeySelector key;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KeySelector {
        private String column;
        private String value;

        public String getColumn() { return column; }
        public void setColumn(String column) { this.column = column; }

        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }

        @Override
        public String toString() {
            return column + "=" + value;
        }
    }

    public String getSheet() { return sheet; }
    public void setSheet(String sheet) { this.sheet = sheet; }

    public Integer getRow() { return row; }
    public void setRow(Integer row) { this.row = row; }

    public String getRows() { return rows; }
    public void setRows(String rows) { this.rows = rows; }

    public KeySelector getKey() { return key; }
    public void setKey(KeySelector key) { this.key = key; }

    /** Human-readable description of the selector used (for reports). */
    public String selectorDescription() {
        String base = "sheet=" + sheet;
        if (row != null)  return base + ", row=" + row;
        if (key != null)  return base + ", " + key;
        if (rows != null) return base + ", rows=" + rows;
        return base + " (first row)";
    }
}
