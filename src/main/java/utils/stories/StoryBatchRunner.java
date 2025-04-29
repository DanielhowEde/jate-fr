package utils.stories;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

public class StoryBatchRunner {

    private static final String STORIES_DIR = "src/test/resources/stories/";

    public static void runAllStories() {
        try {
            System.out.println("🧹 Clearing results folder...");
            utils.excel.ExcelFileManager.clearResultsFolder("results"); // Clear ONCE at batch start
        	
            List<File> storyFiles = findAllStoryJsonFiles(new File(STORIES_DIR));

            if (storyFiles.isEmpty()) {
                System.out.println("⚠️ No story files found in: " + STORIES_DIR);
                return;
            }

            for (File storyFile : storyFiles) {
                System.out.println("\n🧩 Starting story: " + storyFile.getName());

                // Each file will be passed one at a time to StoryReader
                StoryReader.runSingleStory(storyFile);

                // Important: Clean up StoryContext after each story finishes
                utils.context.StoryContext.clear();
            }

            System.out.println("\n✅ All stories executed!");

        } catch (Exception e) {
            throw new RuntimeException("❌ Failed during batch story run", e);
        }
    }

    private static List<File> findAllStoryJsonFiles(File directory) throws Exception {
        return Files.walk(directory.toPath())
                .filter(p -> p.toString().endsWith(".json"))
                .map(p -> p.toFile())
                .collect(Collectors.toList());
    }
}
