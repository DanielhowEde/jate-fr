package utils.stories;

import com.fasterxml.jackson.databind.ObjectMapper;
import runner.StoryDefinition;
import runner.StoryRunner;
import utils.context.StoryContext;
import utils.excel.ExcelFileManager;

import java.io.File;

public class StoryReader {

    public static void runSingleStory(File storyFile) {
        ExcelFileManager excel = null;

        try {
            System.out.println("📖 Reading story: " + storyFile.getPath());

            ObjectMapper mapper = new ObjectMapper();
            StoryDefinition story = mapper.readValue(storyFile, StoryDefinition.class);

            StoryContext.enableStoryMode();
            StoryContext.put("storyName", story.getStoryName());
            StoryContext.put("storyFile", storyFile.getPath());

            if (story.getSpreadsheet() != null && !story.getSpreadsheet().isEmpty()) {
                String spreadsheetName = story.getSpreadsheet().get(0);
                String originalPath = "testdata/" + spreadsheetName;

                File spreadsheetFile = new File(originalPath);
                if (!spreadsheetFile.exists()) {
                    throw new RuntimeException("❌ Spreadsheet not found: " + originalPath);
                }

                String workingCopyPath = ExcelFileManager.createWorkingCopy(originalPath, "results");
                StoryContext.put("spreadsheet", workingCopyPath);
                System.out.println("🧾 Spreadsheet loaded into StoryContext: " + workingCopyPath);

                // 🔄 Create and close here only if you're using directly
                excel = new ExcelFileManager(workingCopyPath, null);
            } else {
                System.out.println("ℹ️ No spreadsheet defined for story: " + story.getStoryName());
            }

            for (String featurePath : story.getFeatures()) {
                StoryRunner.runFeatureFile(featurePath);
            }

        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to run story from file: " + storyFile.getName(), e);
        } finally {
            if (excel != null) {
                excel.close(); // ✅ Now properly released
            }
        }
    }
}
