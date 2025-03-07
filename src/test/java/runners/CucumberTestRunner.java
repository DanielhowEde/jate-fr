package runners;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.*;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features") // ✅ Ensure this matches "src/test/resources/features/"
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "common.stepdefinitions,core") // Step definitions location
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty, listeners.StepLogger")
public class CucumberTestRunner {

    static {
        System.out.println("🔍 Cucumber is searching for feature files in: " + System.getProperty("java.class.path"));
    }
}