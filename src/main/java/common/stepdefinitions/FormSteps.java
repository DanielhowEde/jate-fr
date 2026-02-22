package common.stepdefinitions;

import core.BasePage;
import core.WebDriverManager;
import io.cucumber.java.en.And;
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

    /**
     * Clicks the page's submit button, identified by common submit-button
     * patterns (type="submit", text "Submit", or value "Submit").
     */
    @And("I click the submit button")
    public void clickSubmitButton() {
        clickButtonByName("Submit Form");
    }

    /**
     * Clicks a button (or button-like element) whose visible label matches
     * {@code buttonName}, ignoring any underscored/underlined access-key letters.
     *
     * <p>Matching is done with {@code normalize-space(.)} which collects text
     * from <em>all</em> descendant text nodes, so a button rendered as
     * {@code <button>S<u>u</u>bmit</button>} is found by passing {@code "Submit"}.
     *
     * <p>Strategy order:
     * <ol>
     *   <li>Exact full-text match on {@code button} / {@code input[type=button|submit|reset]}</li>
     *   <li>{@code aria-label} attribute match</li>
     *   <li>{@code title} attribute match</li>
     *   <li>{@code value} attribute match (for {@code <input>} buttons)</li>
     *   <li>{@code role="button"} with matching text</li>
     *   <li>Anchor ({@code <a>}) with matching text</li>
     *   <li>Partial-text contains match on {@code button} (fallback)</li>
     * </ol>
     *
     * <p>Example:
     * <pre>
     *   When I click the button "Save"
     *   When I click the button "Cancel"
     * </pre>
     */
    @When("I click the button {string}")
    public void clickButtonByName(String buttonName) {
        String e = xpathEscape(buttonName);

        String[] strategies = {
            // 1. Exact visible-text match — normalize-space(.) spans child elements
            //    so <button>S<u>u</u>bmit</button> matches "Submit"
            "//button[normalize-space(.)=" + e + "]",
            "//input[@type='button' or @type='submit' or @type='reset'][normalize-space(@value)=" + e + "]",
            // 2. ARIA label
            "//*[@aria-label=" + e + " and (@role='button' or self::button or self::input)]",
            // 3. title attribute
            "//button[@title=" + e + "]",
            "//*[@role='button'][@title=" + e + "]",
            // 4. value attribute (input buttons)
            "//input[@value=" + e + "]",
            // 5. role="button" with matching text
            "//*[@role='button'][normalize-space(.)=" + e + "]",
            // 6. Anchor styled as a button
            "//a[normalize-space(.)=" + e + "]",
            // 7. Partial-text fallback (contains) — lowest priority
            "//button[contains(normalize-space(.)," + e + ")]",
        };

        for (String xpath : strategies) {
            if (page.elementExists(xpath)) {
                page.clickElementBySelector(xpath);
                LOGGER.info("Clicked button '{}' via: {}", buttonName, xpath);
                return;
            }
        }
        throw new RuntimeException("No button found on page with label: '" + buttonName + "'");
    }

    /** Mirrors {@link core.BasePage#xpathEscape} for use within this class. */
    private static String xpathEscape(String text) {
        if (!text.contains("'"))  return "'" + text + "'";
        if (!text.contains("\"")) return "\"" + text + "\"";
        return "concat('" + text.replace("'", "', \"'\", '") + "')";
    }
}
