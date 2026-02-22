package utils.stories.yaml;

/**
 * Records the outcome of executing one item from the story's {@code run} list.
 */
public class ItemResult {

    private final int index;
    private final YamlRunItem item;
    private boolean passed;
    private String error;
    private long durationMs;

    public ItemResult(int index, YamlRunItem item) {
        this.index = index;
        this.item  = item;
    }

    /** 1-based position of this item in the run list. */
    public int getIndex() { return index; }

    public YamlRunItem getItem() { return item; }

    public boolean isPassed() { return passed; }
    public void setPassed(boolean passed) { this.passed = passed; }

    public boolean isFailed() { return !passed; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }

    /** Human-readable one-line summary for logging. */
    @Override
    public String toString() {
        String status = passed ? "PASS" : "FAIL";
        String target = item.getFeature()
            + (item.getScenario() != null ? " [" + item.getScenario() + "]" : "");
        String dataInfo = item.getData() != null ? " | " + item.getData().selectorDescription() : "";
        return String.format("[%d] %s — %s%s (%dms)", index, status, target, dataInfo, durationMs);
    }
}
