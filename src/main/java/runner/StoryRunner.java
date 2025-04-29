package runner;

import io.cucumber.core.cli.Main;

import java.util.ArrayList;
import java.util.List;

public class StoryRunner {

    public static void runFeatureFile(String featurePath) {
        try {
            List<String> args = new ArrayList<>();

            // Glue paths
            args.add("--glue"); args.add("stepdefinitions");
            args.add("--glue"); args.add("common.stepdefinitions");
            args.add("--glue"); args.add("core");

            // Plugins
            args.add("--plugin"); args.add("pretty");
            args.add("--plugin"); args.add("listeners.StepLogger");

            // Feature file
            args.add(featurePath);

            System.out.println("🎬 Running feature: " + featurePath);
            Main.run(args.toArray(new String[0]));

        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to run feature: " + featurePath, e);
        }
    }
}