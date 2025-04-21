package utils.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;


public class ExcelFileManager {
    private Workbook workbook;
    private Sheet sheet;
    private Iterator<Row> rowIterator;
    private List<String> headers;
    private static final Logger logger = Logger.getLogger(ExcelFileManager.class.getName());

    public ExcelFileManager(String filePath, String sheetName) {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            this.workbook = new XSSFWorkbook(fis);
            
            if (sheetName != null && !sheetName.isBlank()) {
                this.sheet = workbook.getSheet(sheetName);
                if (this.sheet == null) {
                    throw new RuntimeException("❌ Sheet not found: " + sheetName);
                }
            } else {
                this.sheet = workbook.getSheetAt(0); // default to first tab
            }
            
            this.rowIterator = sheet.iterator();

            // Extract headers from the first row
            if (rowIterator.hasNext()) {
                Row headerRow = rowIterator.next();
                headers = new ArrayList<>();
                for (Cell cell : headerRow) {
                    headers.add(cell.getStringCellValue().trim());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("❌ Failed to load Excel file: " + filePath, e);
        }
    }

    public Map<String, String> getNextRow() {
        if (!rowIterator.hasNext()) {
            return Collections.emptyMap(); // or throw exception if preferred
        }

        Row row = rowIterator.next();
        Map<String, String> dataMap = new HashMap<>();

        for (int i = 0; i < headers.size(); i++) {
            Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            dataMap.put(headers.get(i), getCellValueAsString(cell));
        }

        return dataMap;
    }

    private String getCellValueAsString(Cell cell) {
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            case BLANK -> "";
            default -> "Unsupported cell type";
        };
    }

    public static String createWorkingCopy(String originalPath, String outputFolder) {
        File originalFile = new File(originalPath);
        if (!originalFile.exists()) {
            throw new RuntimeException("❌ Required Excel file not found: " + originalPath);
        }

        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String outputFileName = originalFile.getName().replace(".xlsx", "_" + timestamp + ".xlsx");

            Path outputPath = Paths.get(outputFolder, outputFileName);
            Files.createDirectories(outputPath.getParent());
            Files.copy(originalFile.toPath(), outputPath, StandardCopyOption.REPLACE_EXISTING);

            return outputPath.toString();
        } catch (IOException e) {
            throw new RuntimeException("❌ Failed to create working copy of Excel file", e);
        }
    }
    
    public static void clearResultsFolder(String resultsFolderPath) {
        try {
            Path resultsPath = Paths.get(resultsFolderPath);
            if (Files.exists(resultsPath)) {
                Files.walk(resultsPath)
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
							logger.warning("⚠️ Could not delete file: " + path);
                        }
                    });
            }
            Files.createDirectories(resultsPath); // recreate if missing
        } catch (IOException e) {
            throw new RuntimeException("❌ Failed to clear results folder: " + resultsFolderPath, e);
        }
    }
    
    public List<Map<String, String>> getAllRows() {
        List<Map<String, String>> allRows = new ArrayList<>();

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Map<String, String> dataMap = new HashMap<>();

            for (int i = 0; i < headers.size(); i++) {
                Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                dataMap.put(headers.get(i), getCellValueAsString(cell));
            }

            allRows.add(dataMap);
        }

        return allRows;
    }
}

