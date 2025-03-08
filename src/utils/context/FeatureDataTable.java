package com.framework.utils.context;

import java.util.HashMap;
import java.util.Map;

public class FeatureDataTable {
    private static final Map<String, Map<String, Object>> featureDataTable = new HashMap<>();
    private static final Map<String, Object> globalDataTable = new HashMap<>();

    // Generate unique key using feature code + scenario code
    private static String getScenarioKey(String featureCode, String scenarioCode) {
        return featureCode + "_" + scenarioCode;
    }

    // Store feature-specific data
    public static void set(String featureCode, String scenarioCode, String column, Object value) {
        String scenarioKey = getScenarioKey(featureCode, scenarioCode);
        featureDataTable
            .computeIfAbsent(scenarioKey, k -> new HashMap<>()) // Ensure the scenario exists
            .put(column, value);
    }

    // Retrieve feature-specific data
    public static Object get(String featureCode, String scenarioCode, String column) {
        String scenarioKey = getScenarioKey(featureCode, scenarioCode);
        return featureDataTable.getOrDefault(scenarioKey, new HashMap<>()).get(column);
    }

    // Check if feature-specific data exists
    public static boolean contains(String featureCode, String scenarioCode, String column) {
        String scenarioKey = getScenarioKey(featureCode, scenarioCode);
        return featureDataTable.containsKey(scenarioKey) &&
               featureDataTable.get(scenarioKey).containsKey(column);
    }

    // Clear all feature-specific stored data
    public static void clearFeatureData() {
        featureDataTable.clear();
    }

    // ✅ NEW: Store global data (shared across all tests)
    public static void setGlobal(String key, Object value) {
        globalDataTable.put(key, value);
    }

    // ✅ NEW: Retrieve global data
    public static Object getGlobal(String key) {
        return globalDataTable.get(key);
    }

    // ✅ NEW: Check if global data exists
    public static boolean containsGlobal(String key) {
        return globalDataTable.containsKey(key);
    }

    // ✅ NEW: Clear all global data
    public static void clearGlobalData() {
        globalDataTable.clear();
    }
}
