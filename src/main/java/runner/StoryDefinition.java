package runner;

import java.util.List;

public class StoryDefinition {

    private String storyName;
    private List<String> spreadsheet; // 🔥 now called "spreadsheet", matches the new JSON
    private List<String> features;

    public StoryDefinition() {}

    public String getStoryName() {
        return storyName;
    }

    public void setStoryName(String storyName) {
        this.storyName = storyName;
    }

    public List<String> getSpreadsheet() {
        return spreadsheet;
    }

    public void setSpreadsheet(List<String> spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    public List<String> getFeatures() {
        return features;
    }

    public void setFeatures(List<String> features) {
        this.features = features;
    }
}
