package runner;

import io.cucumber.core.cli.Main;
import utils.context.StoryContext;
import java.util.ArrayList;
import java.util.List;
import utils.excel.ExcelLoader;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import utils.reports.ExtentManager;

public class StoryRunner {
	
	private static ExtentTest storyLog;

    public static void runStory(StoryDefinition story) {
        beforeStory(story); // 🔁 HOOK: Before Story
        StoryContext.clear();

        for (String feature : story.getFeatures()) {
            runFeature(feature);
        }

        afterStory(story); // 🔁 HOOK: After Story
    }

    private static void beforeStory(StoryDefinition story) {
        StoryContext.enableStoryMode();
        ExtentReports extent = ExtentManager.getExtentReports();
        storyLog = extent.createTest("STORY: " + story.getStoryName());
        storyLog.info("Starting story execution: " + story.getStoryName());
        System.out.println("===== BEGIN STORY: " + story.getStoryName() + " =====");

        if (story.getTags() != null) {
            for (String tag : story.getTags()) {
                if (tag.startsWith("@storyData:")) {
                    String fileName = tag.split(":")[1];
                    ExcelLoader.loadStoryData(fileName); // ✅ Use the utility method
                }
            }
        }
    }

    private static void afterStory(StoryDefinition story) {
        storyLog.pass("Story '" + story.getStoryName() + "' completed successfully.");
        ExtentManager.getExtentReports().flush();
        StoryContext.clear();
        System.out.println("===== END STORY: " + story.getStoryName() + " =====");
        // 🔧 Cleanup logic here (flush logs, reset states, report aggregation)
    }

    private static void runFeature(String featurePath) {
        System.out.println("Running feature: " + featurePath);
        // Features themselves are logged by Cucumber plugins
        List<String> args = new ArrayList<>();
        args.add("--glue");
        args.add("jatefr.stepdefs");
        args.add("src/test/resources/" + featurePath);
        args.add("--plugin");
        args.add("pretty");

        // Optional: Enable ExtentCucumberAdapter plugin if you use it
        args.add("--plugin");
        args.add("com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:");

        Main.run(args.toArray(new String[0]), Thread.currentThread().getContextClassLoader());
    }
}