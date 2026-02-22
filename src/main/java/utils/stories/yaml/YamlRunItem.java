package utils.stories.yaml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * A single item in the story's {@code run} list.
 *
 * <pre>
 * - feature: features/Login/login.feature
 *   scenario: "Successful Login check"   # optional — omit to run all scenarios in the feature
 *   data:
 *     sheet: Credentials
 *     row: 1
 * </pre>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class YamlRunItem {

    private String feature;
    private String scenario;
    private YamlDataMapping data;

    public String getFeature() { return feature; }
    public void setFeature(String feature) { this.feature = feature; }

    /** Optional. When set, Cucumber runs only the scenario matching this name (regex). */
    public String getScenario() { return scenario; }
    public void setScenario(String scenario) { this.scenario = scenario; }

    public YamlDataMapping getData() { return data; }
    public void setData(YamlDataMapping data) { this.data = data; }
}
