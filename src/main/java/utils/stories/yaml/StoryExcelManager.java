package utils.stories.yaml;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Single-workbook, multi-sheet Excel manager for the YAML story runner.
 *
 * <p>Unlike {@code ExcelFileManager}, this class holds the workbook in an instance field
 * (not a static one), so multiple instances do not interfere with each other and multiple
 * sheets from the same file can be accessed from one instance.
 *
 * <p>Usage:
 * <pre>
 *   try (StoryExcelManager mgr = new StoryExcelManager("results/data.xlsx")) {
 *       List&lt;Map&lt;String,String&gt;&gt; rows = mgr.getSheetRows("Users");
 *       Map&lt;String,String&gt;  row  = mgr.getRowByIndex("Credentials", 1);
 *       Map&lt;String,String&gt;  row  = mgr.getRowByKey("Users", "testCaseId", "TC-101");
 *   }
 * </pre>
 */
public class StoryExcelManager implements Closeable {

    private final Workbook workbook;
    private final String filePath;

    /**
     * Opens the XLSX workbook at {@code filePath} and keeps it in memory until {@link #close()}.
     *
     * @throws RuntimeException if the file cannot be opened
     */
    public StoryExcelManager(String filePath) {
        this.filePath = filePath;
        try (FileInputStream fis = new FileInputStream(filePath)) {
            this.workbook = new XSSFWorkbook(fis);
        } catch (IOException e) {
            throw new RuntimeException("Failed to open Excel workbook: " + filePath, e);
        }
    }

    /**
     * Returns all data rows from the named sheet as a list of column→value maps.
     * Row 0 (header) is used as the map keys; rows 1..N are the data.
     *
     * @throws RuntimeException if the sheet does not exist
     */
    public List<Map<String, String>> getSheetRows(String sheetName) {
        Sheet sheet = requireSheet(sheetName);
        List<String> headers = readHeaders(sheet);

        List<Map<String, String>> result = new ArrayList<>();
        for (int r = 1; r <= sheet.getLastRowNum(); r++) {
            Row row = sheet.getRow(r);
            if (row == null) continue;
            result.add(rowToMap(row, headers));
        }
        return result;
    }

    /**
     * Returns a single data row by 1-based index (row 1 = first data row after the header).
     *
     * @throws RuntimeException if the sheet is missing or the row index is out of range
     */
    public Map<String, String> getRowByIndex(String sheetName, int rowIndex) {
        Sheet sheet = requireSheet(sheetName);
        List<String> headers = readHeaders(sheet);

        // rowIndex 1 → physical POI row 1 (row 0 = header)
        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            throw new RuntimeException(
                "Row " + rowIndex + " does not exist in sheet '" + sheetName + "' of " + filePath
                + " (last data row index: " + sheet.getLastRowNum() + ")");
        }
        return rowToMap(row, headers);
    }

    /**
     * Scans the sheet for the first row where {@code column} equals {@code value}.
     *
     * @throws RuntimeException if the sheet is missing, the column header is absent, or no matching row is found
     */
    public Map<String, String> getRowByKey(String sheetName, String column, String value) {
        Sheet sheet = requireSheet(sheetName);
        List<String> headers = readHeaders(sheet);

        int colIndex = headers.indexOf(column);
        if (colIndex < 0) {
            throw new RuntimeException(
                "Column '" + column + "' not found in sheet '" + sheetName + "' of " + filePath
                + ". Available columns: " + headers);
        }

        for (int r = 1; r <= sheet.getLastRowNum(); r++) {
            Row row = sheet.getRow(r);
            if (row == null) continue;
            Cell cell = row.getCell(colIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            if (value.equals(getCellValue(cell))) {
                return rowToMap(row, headers);
            }
        }

        throw new RuntimeException(
            "No row found in sheet '" + sheetName + "' where column '" + column + "' = '" + value + "' (file: " + filePath + ")");
    }

    @Override
    public void close() throws IOException {
        workbook.close();
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private Sheet requireSheet(String sheetName) {
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            List<String> available = new ArrayList<>();
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                available.add(workbook.getSheetName(i));
            }
            throw new RuntimeException(
                "Sheet '" + sheetName + "' not found in " + filePath
                + ". Available sheets: " + available);
        }
        return sheet;
    }

    private List<String> readHeaders(Sheet sheet) {
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            throw new RuntimeException("Sheet '" + sheet.getSheetName() + "' has no header row");
        }
        List<String> headers = new ArrayList<>();
        for (Cell cell : headerRow) {
            headers.add(getCellValue(cell).trim());
        }
        return headers;
    }

    private Map<String, String> rowToMap(Row row, List<String> headers) {
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            map.put(headers.get(i), getCellValue(cell));
        }
        return map;
    }

    private String getCellValue(Cell cell) {
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                double d = cell.getNumericCellValue();
                yield (d == Math.floor(d) && !Double.isInfinite(d))
                    ? String.valueOf((long) d)
                    : String.valueOf(d);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
                CellValue cv = evaluator.evaluate(cell);
                yield switch (cv.getCellType()) {
                    case STRING  -> cv.getStringValue().trim();
                    case NUMERIC -> String.valueOf((long) cv.getNumberValue());
                    case BOOLEAN -> String.valueOf(cv.getBooleanValue());
                    default      -> "";
                };
            }
            case BLANK -> "";
            default    -> "";
        };
    }
}
