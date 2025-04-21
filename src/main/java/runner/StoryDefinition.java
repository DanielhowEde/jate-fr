package runner;

import java.util.List;

public class StoryDefinition {

    private String storyName;
    private List<String> features;
    private List<String> tags; // 🔥 Add this line

    // Constructors
    public StoryDefinition() {}

    public StoryDefinition(String storyName, List<String> features, List<String> tags) {
        this.storyName = storyName;
        this.features = features;
        this.tags = tags;
    }

    // Getters and Setters
    public String getStoryName() {
        return storyName;
    }

    public void setStoryName(String storyName) {
        this.storyName = storyName;
    }

    public List<String> getFeatures() {
        return features;
    }

    public void setFeatures(List<String> features) {
        this.features = features;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}