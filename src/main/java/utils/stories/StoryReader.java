package utils.stories;

import com.fasterxml.jackson.databind.ObjectMapper;
import runner.StoryDefinition;
import runner.StoryRunner;
import utils.context.StoryContext;

import java.io.File;

public class StoryReader {

    public static void runSingleStory(File storyFile) {
        try {
            System.out.println("📖 Reading story: " + storyFile.getPath());

            ObjectMapper mapper = new ObjectMapper();
            StoryDefinition story = mapper.readValue(storyFile, StoryDefinition.class);

            // Enable story mode
            StoryContext.enableStoryMode();
            StoryContext.put("storyName", story.getStoryName());
            StoryContext.put("storyFile", storyFile.getPath());

            // ✅ If spreadsheet is defined
            if (story.getSpreadsheet() != null && !story.getSpreadsheet().isEmpty()) {
                String spreadsheetName = story.getSpreadsheet().get(0); // assume first
                String originalPath = "testdata/" + spreadsheetName;

                File spreadsheetFile = new File(originalPath);
                if (!spreadsheetFile.exists()) {
                    throw new RuntimeException("❌ Spreadsheet not found: " + originalPath);
                }

                String workingCopyPath = utils.excel.ExcelFileManager.createWorkingCopy(originalPath, "results");
                StoryContext.put("spreadsheet", workingCopyPath);
                System.out.println("🧾 Spreadsheet loaded into StoryContext: " + workingCopyPath);
            } else {
                System.out.println("ℹ️ No spreadsheet defined for story: " + story.getStoryName());
            }

            // Run each feature file (assumes full path given)
            for (String featurePath : story.getFeatures()) {
                StoryRunner.runFeatureFile(featurePath);
            }

        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to run story from file: " + storyFile.getName(), e);
        }
    }
}