package utils.stories.yaml;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads and validates a YAML story file into a {@link YamlStoryDefinition}.
 *
 * <p>Resolution order for {@code yamlPath}:
 * <ol>
 *   <li>Classpath resource (prefixed with leading {@code /} or as-is)
 *   <li>File-system path (absolute or relative to working directory)
 * </ol>
 */
public class YamlStoryLoader {

    private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());

    /**
     * Load a story YAML from the given path and validate its structure.
     *
     * @param yamlPath classpath resource path or file-system path to the YAML file
     * @return validated {@link YamlStoryDefinition}
     * @throws RuntimeException if the file cannot be found, parsed, or fails validation
     */
    public static YamlStoryDefinition load(String yamlPath) {
        YamlStoryDefinition story = parse(yamlPath);
        validate(story, yamlPath);
        return story;
    }

    private static YamlStoryDefinition parse(String yamlPath) {
        // 1. Try classpath
        String resourcePath = yamlPath.startsWith("/") ? yamlPath : "/" + yamlPath;
        try (InputStream is = YamlStoryLoader.class.getResourceAsStream(resourcePath)) {
            if (is != null) {
                return MAPPER.readValue(is, YamlStoryDefinition.class);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse story YAML from classpath [" + yamlPath + "]: " + e.getMessage(), e);
        }

        // 2. Try file system
        File file = new File(yamlPath);
        if (!file.exists()) {
            throw new RuntimeException(
                "Story YAML not found on classpath or filesystem: " + yamlPath);
        }
        try {
            return MAPPER.readValue(file, YamlStoryDefinition.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse story YAML [" + yamlPath + "]: " + e.getMessage(), e);
        }
    }

    private static void validate(YamlStoryDefinition story, String yamlPath) {
        List<String> errors = new ArrayList<>();

        if (story.getStory() == null || story.getStory().isBlank()) {
            errors.add("'story' (name) is required");
        }

        if (story.getRun() == null || story.getRun().isEmpty()) {
            errors.add("'run' list must contain at least one item");
        } else {
            for (int i = 0; i < story.getRun().size(); i++) {
                YamlRunItem item = story.getRun().get(i);
                if (item.getFeature() == null || item.getFeature().isBlank()) {
                    errors.add("run[" + i + "].feature is required");
                }
                YamlDataMapping data = item.getData();
                if (data != null) {
                    if (data.getSheet() == null || data.getSheet().isBlank()) {
                        errors.add("run[" + i + "].data.sheet is required when data is specified");
                    }
                    if (data.getKey() != null) {
                        if (data.getKey().getColumn() == null || data.getKey().getColumn().isBlank()) {
                            errors.add("run[" + i + "].data.key.column is required");
                        }
                        if (data.getKey().getValue() == null) {
                            errors.add("run[" + i + "].data.key.value is required");
                        }
                    }
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new RuntimeException(
                "Story YAML validation failed [" + yamlPath + "]:\n  - " + String.join("\n  - ", errors));
        }
    }
}
