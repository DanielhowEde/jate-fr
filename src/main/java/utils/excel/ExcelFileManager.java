// utils/excel/ExcelFileManager.java
package utils.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class ExcelFileManager implements AutoCloseable {
    private final Path excelPath;
    private final String sheetName;
    private final Workbook workbook;       // single workbook per manager
    private final Sheet sheet;             // single sheet view
    private final DataFormatter formatter = new DataFormatter();

    // Optional caches (drop if you prefer streaming)
    private volatile List<String> headers;
    private volatile List<Map<String,String>> allRows;

    public ExcelFileManager(String excelPath, String sheetName) {
        try {
            this.excelPath = Path.of(excelPath);
            this.sheetName = sheetName;

            // Open workbook directly from path — POI opens its own stream internally
            // (No extra InputStream field to leak)
            this.workbook = new XSSFWorkbook(this.excelPath.toFile());
            Sheet maybe = workbook.getSheet(sheetName);
            if (maybe == null) {
                throw new IllegalArgumentException("Sheet not found: " + sheetName);
            }
            this.sheet = maybe;

            // Lazy: headers/rows loaded on first access to avoid upfront memory spike
            this.headers = null;
            this.allRows = null;
        } catch (IOException ioe) {
            throw new RuntimeException("Failed to open Excel: " + excelPath + " sheet: " + sheetName, ioe);
        }
    }

    private void ensureLoaded() {
        if (headers != null && allRows != null) return;

        synchronized (this) {
            if (headers != null && allRows != null) return;

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) throw new IllegalStateException("No header row (row 0) in sheet: " + sheetName);

            List<String> h = new ArrayList<>();
            for (Cell cell : headerRow) {
                h.add(formatter.formatCellValue(cell).trim());
            }

            List<Map<String,String>> rows = new ArrayList<>();
            int lastRow = sheet.getLastRowNum();
            for (int r = 1; r <= lastRow; r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;
                Map<String,String> map = new LinkedHashMap<>();
                for (int c = 0; c < h.size(); c++) {
                    Cell cell = row.getCell(c, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    String val = (cell == null) ? "" : formatter.formatCellValue(cell);
                    map.put(h.get(c), val);
                }
                rows.add(map);
            }

            this.headers = Collections.unmodifiableList(h);
            this.allRows = Collections.unmodifiableList(rows);
        }
    }

    public List<String> getHeaders() {
        ensureLoaded();
        return headers;
    }

    public List<Map<String,String>> getAllRows() {
        ensureLoaded();
        return allRows;
    }

    public Map<String,String> getRowByIteration(String iteration) {
        ensureLoaded();
        // assume a header named "Iteration" (adjust as needed)
        String key = "Iteration";
        for (Map<String,String> row : allRows) {
            if (iteration.equals(row.get(key))) return row;
        }
        return null;
    }

    public Map<String,String> getRowByIteration(String iteration, String parentIteration) {
        ensureLoaded();
        // assume headers "Iteration" and "ParentID" (adjust names to your sheet)
        String iterKey = "Iteration";
        String parentKey = "ParentID";
        for (Map<String,String> row : allRows) {
            if (iteration.equals(row.get(iterKey)) && parentIteration.equals(row.get(parentKey))) return row;
        }
        return null;
    }

    @Override
    public void close() {
        try {
            workbook.close();
        } catch (IOException e) {
            // log if you wish
        }
    }
}