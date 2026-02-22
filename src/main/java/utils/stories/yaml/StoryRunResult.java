package utils.stories.yaml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Aggregates all {@link ItemResult}s from a single story execution and provides
 * summary statistics.
 */
public class StoryRunResult {

    private final String storyName;
    private final String excelFile;
    private final List<ItemResult> items = new ArrayList<>();

    public StoryRunResult(String storyName, String excelFile) {
        this.storyName = storyName;
        this.excelFile = excelFile;
    }

    public void addItem(ItemResult result) {
        items.add(result);
    }

    public String getStoryName() { return storyName; }

    public String getExcelFile() { return excelFile; }

    public List<ItemResult> getItems() { return Collections.unmodifiableList(items); }

    public int getTotalCount()  { return items.size(); }

    public long getPassCount()  { return items.stream().filter(ItemResult::isPassed).count(); }

    public long getFailCount()  { return items.stream().filter(ItemResult::isFailed).count(); }

    public boolean isAllPassed() { return getFailCount() == 0 && getTotalCount() > 0; }

    public long getTotalDurationMs() {
        return items.stream().mapToLong(ItemResult::getDurationMs).sum();
    }

    @Override
    public String toString() {
        return String.format(
            "Story '%s' — %d/%d passed (%dms total)",
            storyName, getPassCount(), getTotalCount(), getTotalDurationMs());
    }
}
