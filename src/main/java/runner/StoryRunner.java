package runner;

import io.cucumber.core.cli.Main;

import java.util.ArrayList;
import java.util.List;

import static core.TestLogger.LOGGER;

public class StoryRunner {

    /**
     * Runs all scenarios in the given feature file.
     *
     * @return Cucumber exit code (0 = all passed, non-zero = failures)
     */
    public static byte runFeatureFile(String featurePath) {
        return runFeatureFile(featurePath, null);
    }

    /**
     * Runs the feature file, optionally filtering to a single named scenario.
     *
     * @param featurePath  path to the .feature file (classpath or file-system)
     * @param scenarioName exact scenario name to match via {@code --name}, or {@code null} to run all
     * @return Cucumber exit code (0 = all passed, non-zero = failures)
     */
    public static byte runFeatureFile(String featurePath, String scenarioName) {
        List<String> args = new ArrayList<>();

        // Glue paths
        args.add("--glue"); args.add("stepdefinitions");
        args.add("--glue"); args.add("common.stepdefinitions");
        args.add("--glue"); args.add("core");

        // Plugins
        args.add("--plugin"); args.add("pretty");
        args.add("--plugin"); args.add("listeners.StepLogger");

        // Optional scenario name filter
        if (scenarioName != null && !scenarioName.isBlank()) {
            args.add("--name");
            args.add(scenarioName);
        }

        // Feature file
        args.add(featurePath);

        LOGGER.info("Running feature: {}{}", featurePath,
            scenarioName != null ? " [scenario: " + scenarioName + "]" : "");

        return Main.run(args.toArray(new String[0]));
    }
}
