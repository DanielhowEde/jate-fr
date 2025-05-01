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
    private static Workbook workbook;
    private Sheet sheet;
    private List<String> headers;
    private List<Map<String, String>> allRows; // 🚀 preload ALL rows
    private Iterator<Row> rowIterator;
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
                this.sheet = workbook.getSheetAt(0);
            }

            // Read headers
            Row headerRow = sheet.getRow(0);
            headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(cell.getStringCellValue().trim());
            }

            // Read all rows immediately
            allRows = new ArrayList<>();
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;
                Map<String, String> dataRow = new HashMap<>();
                for (int c = 0; c < headers.size(); c++) {
                    Cell cell = row.getCell(c, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    dataRow.put(headers.get(c), getCellValueAsString(cell));
                }
                allRows.add(dataRow);
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
    
    public List<Map<String, String>> getAllRows() {
        return allRows; // ✅ always return cached rows
    }

    private String getCellValueAsString(Cell cell) {
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                CellValue evaluatedValue = evaluator.evaluate(cell);
                yield switch (evaluatedValue.getCellType()) {
                    case STRING -> evaluatedValue.getStringValue().trim();
                    case NUMERIC -> String.valueOf((int) evaluatedValue.getNumberValue());
                    case BOOLEAN -> String.valueOf(evaluatedValue.getBooleanValue());
                    case BLANK -> "";
                    default -> "Unsupported formula type";
                };
            }
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
    
    
    public static void cleanOldResults(String resultsFolderPath, long maxAgeHours) {
        try {
            Path resultsPath = Paths.get(resultsFolderPath);
            if (Files.exists(resultsPath)) {
                long cutoffTime = System.currentTimeMillis() - (maxAgeHours * 60 * 60 * 1000);

                Files.walk(resultsPath)
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        try {
                            return Files.getLastModifiedTime(path).toMillis() < cutoffTime;
                        } catch (IOException e) {
                            return false;
                        }
                    })
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            System.out.println("🗑️ Deleted old result file: " + path);
                        } catch (IOException e) {
                            System.err.println("⚠️ Could not delete file: " + path);
                        }
                    });
            }
        } catch (IOException e) {
            throw new RuntimeException("❌ Failed to clean old results: " + resultsFolderPath, e);
        }
    }
    
    public void close() {
        try {
            if (workbook != null) {
                workbook.close();
            }
        } catch (IOException e) {
            logger.warning("⚠️ Failed to close workbook: " + e.getMessage());
        }
    }
}

