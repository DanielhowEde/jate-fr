package tools.stepdefinitiongenerator;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.regex.*;

public class StepDefinitionGenerator {

    public static void main(String[] args) throws IOException {
        Path featureDir = Paths.get("src/test/resources/features"); // adjust if needed
        Files.walkFileTree(featureDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.toString().endsWith(".feature")) {
                    processFeatureFile(file);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static void processFeatureFile(Path featureFile) {
        List<String> steps = new ArrayList<>();
        Pattern pattern = Pattern.compile("^(Given|When|Then|And|But)\\s+(.+)");
        try (BufferedReader reader = Files.newBufferedReader(featureFile)) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line.trim());
                if (matcher.find()) {
                    steps.add(matcher.group(1) + " " + matcher.group(2));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!steps.isEmpty()) {
            generateStepDefinitionClass(featureFile, steps);
            generatePOMClass(featureFile);
        }
    }

    private static void generateStepDefinitionClass(Path featureFile, List<String> steps) {
        String folderName = featureFile.getParent().getFileName().toString();
        String className = toPascalCase(folderName) + "StepDefinitions";
        Path stepDefFile = featureFile.getParent().resolve(className + ".java");

        // Skip if file already exists and is non-empty
        if (Files.exists(stepDefFile) && stepDefFile.toFile().length() > 0) {
            System.out.println("Skipping existing StepDefinition: " + stepDefFile.getFileName());
            return;
        }

        try (BufferedWriter writer = Files.newBufferedWriter(stepDefFile)) {
            writer.write("package stepdefinitions;\n\n");
            writer.write("import io.cucumber.java.en.*;\n\n");
            writer.write("public class " + className + " {\n\n");

            for (String step : steps) {
                String annotation = step.split(" ")[0];
                String rawStep = step.substring(annotation.length()).trim();

                // Replace quoted strings with {string}
                String annotationText = rawStep.replaceAll("\"[^\"]*\"", "{string}");

                // Count number of quoted items
                int paramCount = rawStep.split("\"").length / 2;
                StringBuilder params = new StringBuilder();
                for (int i = 1; i <= paramCount; i++) {
                    if (i > 1) params.append(", ");
                    params.append("String value").append(i);
                }

                // If only one param, don't add number suffix
                String paramSignature = (paramCount == 1) ? "String value" : params.toString();

                // Method name
                String methodName = toCamelCase(annotationText);

                writer.write("    @" + annotation + "(\"" + annotationText + "\")\n");
                writer.write("    public void " + methodName + "(" + paramSignature + ") {\n");
                writer.write("        // TODO: Implement step\n");
                writer.write("    }\n\n");
            }

            writer.write("}\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void generatePOMClass(Path featureFile) {
        String folderName = featureFile.getParent().getFileName().toString();
        String className = toPascalCase(folderName) + "POM";
        Path pomFile = featureFile.getParent().resolve(className + ".java");

        if (Files.exists(pomFile) && pomFile.toFile().length() > 0) {
            System.out.println("Skipping existing POM: " + pomFile.getFileName());
            return;
        } else {
            try (BufferedWriter writer = Files.newBufferedWriter(pomFile)) {
                writer.write("package pageobjects;\n\n");
                writer.write("public class " + className + " {\n");
                writer.write("    // Page elements and actions for " + folderName + "\n");
                writer.write("}\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String toPascalCase(String text) {
        String[] parts = text.split("[_\\-\\s]+");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                sb.append(part.substring(0, 1).toUpperCase());
                if (part.length() > 1) sb.append(part.substring(1).toLowerCase());
            }
        }
        return sb.toString();
    }

    private static String toCamelCase(String text) {
        String[] parts = text.replaceAll("[^a-zA-Z0-9\\s]", "").split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].isEmpty()) continue;
            if (i == 0) {
                sb.append(parts[i].toLowerCase());
            } else {
                sb.append(parts[i].substring(0, 1).toUpperCase())
                  .append(parts[i].substring(1).toLowerCase());
            }
        }
        return sb.toString();
    }
}
