package jatefr.test.framework;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DynamicTestRunner {

    public static void main(String[] args) throws IOException, InterruptedException {
        File baseDir = new File(System.getProperty("user.dir"));
        File[] testRepos = baseDir.listFiles((dir, name) -> name.endsWith("-tests") && new File(dir, name).isDirectory());

        if (testRepos == null || testRepos.length == 0) {
            System.out.println("❌ No '-tests' repos found.");
            return;
        }

        for (File repo : testRepos) {
            System.out.println("\n🚀 Running tests in repo: " + repo.getName());

            List<String> command = new ArrayList<>();
            command.add("java");
            command.add("-cp");
            command.add(System.getProperty("java.class.path"));
            command.add("io.cucumber.core.cli.Main");
            command.add("--glue");
            command.add("commonsteps,tests.stepdefinitions"); // Include shared + local
            command.add("--plugin");
            command.add("null"); // ✅ disables default Cucumber output plugins
            command.add("src/test/resources/features");

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(repo);
            pb.inheritIO();

            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("✅ Tests passed for: " + repo.getName());
            } else {
                System.err.println("❌ Tests failed for: " + repo.getName());
            }
        }
    }
} 
