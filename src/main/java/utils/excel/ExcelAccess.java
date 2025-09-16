// utils/excel/ExcelAccess.java
package utils.excel;

import core.ContextManager;       // your existing context store
import core.TestLogger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class ExcelAccess {
    private static final String SHEET_CACHE_KEY = "sheetCache";
    private ExcelAccess() {}

    @SuppressWarnings("unchecked")
    private static Map<String, ExcelFileManager> cache() {
        Map<String, ExcelFileManager> sheetCache;
        if (!ContextManager.contains("sheetCache")) {
            sheetCache = new ConcurrentHashMap<>();
            ContextManager.put(SHEET_CACHE_KEY, sheetCache);
        } else {
            sheetCache = (Map<String, ExcelFileManager>) ContextManager.get(SHEET_CACHE_KEY);
        }
        return sheetCache;
    }

    public static ExcelFileManager getSheet(String sheetName) {
        String spreadsheetPath = (String) ContextManager.get("spreadsheet");
        if (spreadsheetPath == null || spreadsheetPath.isBlank()) {
            throw new IllegalStateException("Context 'spreadsheet' path not set.");
        }

        Map<String, ExcelFileManager> sheetCache = cache();

        // computeIfAbsent prevents races and double-opens
        return sheetCache.computeIfAbsent(sheetName, name -> {
            TestLogger.LOGGER.info("📘 Opening Excel sheet: " + name);
            return new ExcelFileManager(spreadsheetPath, name);
        });
    }

    public static Map<String,String> getRowByIteration(String sheetName, String iteration, Map<String,Object> context) {
        ExcelFileManager excel = getSheet(sheetName);
        if (context != null && context.containsKey("ParentID")) {
            String parentIteration = String.valueOf(context.get("ParentID"));
            return excel.getRowByIteration(iteration, parentIteration);
        } else {
            return excel.getRowByIteration(iteration);
        }
    }

    public static Map<String,String> getSpreadsheetRow(int index, String sheetName) {
        try {
            ExcelFileManager excel = getSheet(sheetName);
            List<Map<String,String>> rows = excel.getAllRows();

            if (rows == null || rows.isEmpty()) {
                TestLogger.LOGGER.info("📌 No data for: " + sheetName + " on row index " + index + " — skipping");
                return null;
            }
            if (index < 0 || index >= rows.size()) {
                TestLogger.LOGGER.info("📌 " + sheetName + " row " + index + " is out of range — skipping");
                return null;
            }
            return rows.get(index);
        } catch (Exception e) {
            TestLogger.LOGGER.info("📌 Sheet " + sheetName + " does not exist — skipping");
            return null;
        }
    }

    /** Close and remove one sheet from cache (e.g., when reloading). */
    public static void closeSheet(String sheetName) {
        Map<String, ExcelFileManager> sheetCache = cache();
        ExcelFileManager mgr = sheetCache.remove(sheetName);
        if (mgr != null) {
            try {
                mgr.close();
            } catch (Exception ignored) {}
            TestLogger.LOGGER.info("🧹 Closed sheet: " + sheetName);
        }
    }

    /** Clear and close all cached sheets — call from your global teardown (e.g., @AfterAll). */
    public static void closeAll() {
        Map<String, ExcelFileManager> sheetCache = cache();
        for (Map.Entry<String, ExcelFileManager> e : sheetCache.entrySet()) {
            try { e.getValue().close(); } catch (Exception ignored) {}
        }
        sheetCache.clear();
        TestLogger.LOGGER.info("🧹 Cleared Excel sheet cache");
    }

    /** Replace a sheet instance (e.g., spreadsheet path changed). */
    public static void reloadSheet(String sheetName) {
        closeSheet(sheetName);
        getSheet(sheetName); // will open fresh
    }
}