package com.tests;

import com.framework.utils.context.FeatureDataTable;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // Ensures tests run in a specific order
public class FeatureDataTableTest {
    
    private static final String FEATURE_CODE = "CODE_LoginFeature";
    private static final String SCENARIO_CODE = "CODE_StoreUserData";
    
    @BeforeEach
    public void setup() {
        FeatureDataTable.clearFeatureData();  // Ensure clean state before each test
        FeatureDataTable.clearGlobalData();
    }

    @Test
    @Order(1)
    @DisplayName("Test Storing and Retrieving Feature-Specific Data")
    public void testStoreAndRetrieveFeatureData() {
        FeatureDataTable.set(FEATURE_CODE, SCENARIO_CODE, "username", "testUser");
        FeatureDataTable.set(FEATURE_CODE, SCENARIO_CODE, "password", "securePass");

        assertEquals("testUser", FeatureDataTable.get(FEATURE_CODE, SCENARIO_CODE, "username"), "❌ Username mismatch!");
        assertEquals("securePass", FeatureDataTable.get(FEATURE_CODE, SCENARIO_CODE, "password"), "❌ Password mismatch!");
    }

    @Test
    @Order(2)
    @DisplayName("Test Feature-Specific Data Not Present")
    public void testFeatureDataNotPresent() {
        assertNull(FeatureDataTable.get(FEATURE_CODE, SCENARIO_CODE, "nonExistingKey"), "❌ Expected null for missing key!");
    }

    @Test
    @Order(3)
    @DisplayName("Test Overwriting Existing Feature Data")
    public void testOverwriteFeatureData() {
        FeatureDataTable.set(FEATURE_CODE, SCENARIO_CODE, "email", "oldEmail@example.com");
        FeatureDataTable.set(FEATURE_CODE, SCENARIO_CODE, "email", "newEmail@example.com");

        assertEquals("newEmail@example.com", FeatureDataTable.get(FEATURE_CODE, SCENARIO_CODE, "email"), "❌ Failed to overwrite existing data!");
    }

    @Test
    @Order(4)
    @DisplayName("Test Global Data Storage and Retrieval")
    public void testGlobalDataStorage() {
        FeatureDataTable.setGlobal("globalKey", "globalValue");

        assertEquals("globalValue", FeatureDataTable.getGlobal("globalKey"), "❌ Global value mismatch!");
    }

    @Test
    @Order(5)
    @DisplayName("Test Global Data Not Present")
    public void testGlobalDataNotPresent() {
        assertNull(FeatureDataTable.getGlobal("missingGlobalKey"), "❌ Expected null for missing global key!");
    }

    @Test
    @Order(6)
    @DisplayName("Test Clearing Feature-Specific Data")
    public void testClearFeatureData() {
        FeatureDataTable.set(FEATURE_CODE, SCENARIO_CODE, "sessionID", "12345");
        FeatureDataTable.clearFeatureData();

        assertNull(FeatureDataTable.get(FEATURE_CODE, SCENARIO_CODE, "sessionID"), "❌ Feature data not cleared properly!");
    }

    @Test
    @Order(7)
    @DisplayName("Test Clearing Global Data")
    public void testClearGlobalData() {
        FeatureDataTable.setGlobal("globalToken", "secureToken");
        FeatureDataTable.clearGlobalData();

        assertNull(FeatureDataTable.getGlobal("globalToken"), "❌ Global data not cleared properly!");
    }
}
