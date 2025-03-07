package utils.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class ExcelReader {

    public static List<Map<String, String>> readExcel(String filePath, String sheetName) {
        List<Map<String, String>> dataList = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) throw new RuntimeException("❌ Sheet not found: " + sheetName);

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) throw new RuntimeException("❌ No header row in Excel file");

            int rowCount = sheet.getPhysicalNumberOfRows();
            int columnCount = headerRow.getLastCellNum();

            for (int i = 1; i < rowCount; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Map<String, String> rowData = new HashMap<>();
                for (int j = 0; j < columnCount; j++) {
                    String key = headerRow.getCell(j).getStringCellValue();
                    Cell cell = row.getCell(j);
                    String value = cell != null ? getCellValue(cell) : "";
                    rowData.put(key, value);
                }
                dataList.add(rowData);
            }

        } catch (IOException e) {
            throw new RuntimeException("❌ Failed to read Excel file: " + filePath, e);
        }

        return dataList;
    }

    private static String getCellValue(Cell cell) {
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }
}
