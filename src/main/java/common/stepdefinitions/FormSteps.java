package common.stepdefinitions;

import core.BasePage;
import core.WebDriverManager;
import io.cucumber.java.en.When;
import org.openqa.selenium.WebDriver;
import utils.context.ContextManager;
import utils.stories.yaml.StoryExcelManager;

import java.io.IOException;
import java.util.Map;

import static core.TestLogger.LOGGER;

public class FormSteps {

    private static final String ITERATION_COLUMN = "Iteration";

    private final WebDriver driver = WebDriverManager.getDriver();
    private final BasePage page = new BasePage(driver);

    /**
     * Fills all on-screen form fields using a single data row from the named
     * Excel sheet. The row is selected by matching the fixed {@code "Iteration"}
     * column to {@code iteration}. Every other column header is treated as a
     * visible page label; its cell value is entered into the matching field.
     *
     * <p>Empty cell values are silently skipped (field left as-is on the page).
     * Values {@code SKIP} or {@code NULL} (case-insensitive) are also skipped.
     * Column headers that don't match any label on the current page are silently
     * skipped with a warning log entry.
     *
     * <p>The Excel file must already be in context — loaded either by a
     * {@code @Data_<FileName>} tag (Hooks) or by the YAML story runner.
     *
     * <p>Example feature file usage:
     * <pre>
     *   When I complete all fields on the page using "FormData" for "1"
     * </pre>
     *
     * <p>Excel sheet structure (sheet name = first parameter):
     * <pre>
     *   | Iteration | Name   | Are you a person | I have a pet |
     *   | 1         | MyName | Check            | Click        |
     * </pre>
     */
    @When("I complete all fields on the page using {string} for {string}")
    public void completeAllFieldsOnPage(String sheetName, String iteration) {
        String excelPath = (String) ContextManager.get("spreadsheet");
        if (excelPath == null) {
            throw new RuntimeException(
                "No Excel file found in context. Add a @Data_<FileName> tag to the scenario " +
                "or run via the YAML story runner.");
        }

        LOGGER.info("Filling form from sheet '{}' iteration '{}'", sheetName, iteration);

        try (StoryExcelManager excel = new StoryExcelManager(excelPath)) {
            Map<String, String> row = excel.getRowByKey(sheetName, ITERATION_COLUMN, iteration);

            for (Map.Entry<String, String> entry : row.entrySet()) {
                String label = entry.getKey().trim();
                String value = entry.getValue().trim();

                // Skip the Iteration column — it is a row selector, not a form field
                if (label.equalsIgnoreCase(ITERATION_COLUMN)) continue;

                // Empty cell → silently skip, field left untouched on the page
                if (value.isEmpty()) continue;

                // Explicit exclusion markers → silently skip
                if (value.equalsIgnoreCase("SKIP") || value.equalsIgnoreCase("NULL")) continue;

                try {
                    page.fillFieldByLabel(label, value);
                } catch (RuntimeException e) {
                    // Column header has no matching label on the current page — warn and move on
                    LOGGER.warn("Skipping column '{}': no matching field found on page — {}",
                        label, e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to close Excel workbook after form fill", e);
        }

        LOGGER.info("Form fill complete for sheet '{}' iteration '{}'", sheetName, iteration);
    }
}
