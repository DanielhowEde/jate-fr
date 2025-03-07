package tools.createfolders;

import java.nio.file.Files;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;



public class createFolder {
	
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("❌ Please provide an app name as a command line argument.");
            return;
        }

        String appName = args[0].toLowerCase().replaceAll("\\s+", "");
        String root = "./";
        String enginePath = root + "/jate-fr";
        String appPath = root + "/" + appName + "-tests";

        //createDir(enginePath);
        //createEnginePom(enginePath);

        createAppStructure(appPath, appName);
        updateTestFolderList(appName);
        
        System.out.println("\n✅ Project initialized for app: " + appName);
    }

    // Create directory
    static void createDir(String path) {
        File dir = new File(path);
        if (dir.mkdirs()) {
            System.out.println("Created: " + path);
        }
    }

    // Write file content
    static void writeFile(String path, String content) {
        try (FileWriter writer = new FileWriter(path)) {
            writer.write(content);
            System.out.println("Created file: " + path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Engine placeholder
    static void createEnginePom(String path) {
        createDir(path + "/src/test/java");
        writeFile(path + "/pom.xml", getEnginePom());
        writeFile(path + "/src/test/java/TestRunner.java", getEngineTestRunner());
    }

    // App structure with feature + step + runner
    static void createAppStructure(String appPath, String appName) {
        String[] subDirs = {
            "/src/test/java/runner",
            "/src/test/java/steps",
            "/src/test/java/stepdefinitions",
            "/src/test/resources/features"
        };

        for (String sub : subDirs) {
            createDir(appPath + sub);
        }

        writeFile(appPath + "/pom.xml", getAppPom(appName));
        writeFile(appPath + "/src/test/java/runner/CucumberTestRunner.java", getCucumberTestRunner());
        writeFile(appPath + "/src/test/java/stepdefinitions/SampleSteps.java", getSampleSteps());
        writeFile(appPath + "/src/test/resources/features/sample.feature", getSampleFeature());
        writeFile(appPath + "/src/test/resources/config.properties", getConfigProperties(appName));
    }

    // Engine POM
    static String getEnginePom() {
        return
            "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
            "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0\n" +
            "         http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
            "    <modelVersion>4.0.0</modelVersion>\n" +
            "    <groupId>DanielhowEde</groupId>\n" +
            "    <artifactId>jatefr</artifactId>\n" +
            "    <version>0.0.1-SNAPSHOT</version>\n" +
            "    <packaging>jar</packaging>\n" +
            "</project>\n";
    }

    // App POM
    static String getAppPom(String appName) {
        String artifactId = appName.toLowerCase().replaceAll("\\s+", "") + "-tests";
        return
            "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
            "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0\n" +
            "         http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
            "    <modelVersion>4.0.0</modelVersion>\n" +
            "    <groupId>com.jatefr</groupId>\n" +
            "    <artifactId>" + artifactId + "</artifactId>\n" +
            "    <version>1.0.0</version>\n" +
            "    <dependencies>\n" +
            "        <dependency>\n" +
            "            <groupId>DanielhowEde</groupId>\n" +
            "            <artifactId>jatefr</artifactId>\n" +
            "            <version>0.0.1-SNAPSHOT</version>\n" +
            "            <scope>test</scope>\n" +
            "        </dependency>\n" +
            "    </dependencies>\n" +
            "</project>\n";
    }

    // App test runner (JUnit + Cucumber)
    static String getCucumberTestRunner() {
        return
            "package runner;\n\n" +
            "import org.junit.platform.suite.api.ConfigurationParameter;\n" +
            "import org.junit.platform.suite.api.IncludeEngines;\n" +
            "import org.junit.platform.suite.api.SelectClasspathResource;\n" +
            "import org.junit.platform.suite.api.Suite;\n\n" +
            "import static io.cucumber.junit.platform.engine.Constants.*;\n\n" +
            "@Suite\n" +
            "@IncludeEngines(\"cucumber\")\n" +
            "@SelectClasspathResource(\"features\")\n" +
            "@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = \"stepdefinitions,common.stepdefinitions,core\")\n" +
            "@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = \"pretty, listeners.StepLogger\")\n" +
            "public class CucumberTestRunner {\n" +
            "    static {\n" +
            "        System.out.println(\"🔍 Cucumber glue paths: stepdefinitions, common.stepdefinitions, core\");\n" +
            "    }\n" +
            "}\n";
    }

    // Sample feature file
    static String getSampleFeature() {
        return
            "Feature: Sample feature\n\n" +
            "  Scenario: Simple test\n" +
            "    Given I print \"Hello from feature file!\"\n";
    }

    // Sample step definition
    static String getSampleSteps() {
        return
            "package stepdefinitions;\n\n" +
            "import io.cucumber.java.en.Given;\n\n" +
            "public class SampleSteps {\n\n" +
            "    @Given(\"I print {string}\")\n" +
            "    public void iPrint(String message) {\n" +
            "        System.out.println(\"📢 Step says: \" + message);\n" +
            "    }\n" +
            "}\n";
    }

    // Optional test-engine placeholder runner
    static String getEngineTestRunner() {
        return
            "public class TestRunner {\n" +
            "    public static void main(String[] args) {\n" +
            "        System.out.println(\"Test engine ready.\");\n" +
            "    }\n" +
            "}\n";
    }
    
    // Generates the config.properties file
    static String getConfigProperties(String appName) {
        return
            "browser=chrome\n" +
            "headless=false\n" +
            "report.name="+ appName +"\n";
    }
    
    static void updateTestFolderList(String appName) {
        String folder = appName.toLowerCase().replaceAll("\\s+", "") + "-tests";
        File listFile = new File("./docker/test-folders.list");
        
        try {
            listFile.getParentFile().mkdirs(); // ensure /docker exists
            if (!listFile.exists()) {
                listFile.createNewFile();
            }

            // Only add if not already listed
            boolean alreadyExists = Files.readAllLines(listFile.toPath()).stream()
                .anyMatch(line -> line.trim().equals(folder));
            
            if (!alreadyExists) {
                try (FileWriter writer = new FileWriter(listFile, true)) {
                    writer.write(folder + "\n");
                    System.out.println("✅ Added to test list: " + folder);
                }
            }

        } catch (IOException e) {
            System.err.println("❌ Failed to update test folder list: " + e.getMessage());
        }
    }
    
}