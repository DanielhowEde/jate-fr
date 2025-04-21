package utils.excel;

import utils.context.StoryContext;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import java.io.FileInputStream;
import java.io.IOException;

public class ExcelLoader {

    public static void loadStoryData(String fileName) {
        String path = "src/test/resources/testdata/" + fileName;

        try (FileInputStream fis = new FileInputStream(path);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getPhysicalNumberOfCells() >= 2) {
                    String key = row.getCell(0).getStringCellValue();
                    String value = row.getCell(1).getStringCellValue();
                    StoryContext.put(key, value);
                }
            }

            System.out.println("✅ Loaded story data from Excel: " + fileName);

        } catch (IOException e) {
            System.err.println("❌ Failed to load Excel data: " + fileName);
            e.printStackTrace();
        }
    }
}