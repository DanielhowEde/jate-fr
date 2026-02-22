package utils.stories.yaml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Top-level DTO for a YAML story file.
 *
 * <pre>
 * story: My Story Name
 * description: Optional description
 * excel: testdata/mydata.xlsx
 * settings:
 *   failFast: false
 * run:
 *   - feature: features/Login/login.feature
 *     data:
 *       sheet: Credentials
 *       row: 1
 * </pre>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class YamlStoryDefinition {

    private String story;
    private String description;
    private String excel;
    private Settings settings;
    private List<YamlRunItem> run;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Settings {
        private boolean failFast = false;

        public boolean isFailFast() { return failFast; }
        public void setFailFast(boolean failFast) { this.failFast = failFast; }
    }

    public String getStory() { return story; }
    public void setStory(String story) { this.story = story; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getExcel() { return excel; }
    public void setExcel(String excel) { this.excel = excel; }

    public Settings getSettings() { return settings; }
    public void setSettings(Settings settings) { this.settings = settings; }

    public List<YamlRunItem> getRun() { return run; }
    public void setRun(List<YamlRunItem> run) { this.run = run; }
}
