package utils.api;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemplateLoader {
	private static final Pattern VAR = Pattern.compile("\\$\\{([a-zA-Z0-9_.:-]+)}");
	private static final Pattern RAW_VAR = Pattern.compile("\\$\\{\\{([a-zA-Z0-9_.:-]+)}}"); // ${{var}} raw insert
	private static final ObjectMapper MAPPER = new ObjectMapper();

	public static String load(String resourcePath) {
		try {
			return new String(Files.readAllBytes(Paths.get(resourcePath)));
		} catch (Exception e) {
			try (var in = TemplateLoader.class.getResourceAsStream(resourcePath)) {
				if (in == null)
					throw new RuntimeException("Classpath resource not found: " + resourcePath);
				return new String(in.readAllBytes());
			} catch (Exception ex) {
				throw new RuntimeException("Cannot load template: " + resourcePath, ex);
			}
		}
	}

	public static String render(String template, Map<String, Object> vars, boolean validateJson) {
// 1) dates, 2) ${var}/raw, (cond handled before)
		String withDates = DateFunctionTemplater.render(template, vars);
		String filled = apply(withDates, vars);
		if (validateJson)
			assertValidJson(filled);
		return filled;
	}

	public static String apply(String template, Map<String, Object> vars) {
// raw insert first
		String t = RAW_VAR.matcher(template).replaceAll(mr -> {
			String key = mr.group(1);
			Object val = vars.get(key);
			return val == null ? "null" : String.valueOf(val);
		});
// then normal ${var[:default]}
		return VAR.matcher(t).replaceAll(mr -> {
			String key = mr.group(1);
			String name = key;
			String def = null;
			int c = key.indexOf(':');
			if (c > -1) {
				name = key.substring(0, c);
				def = key.substring(c + 1);
			}
			Object val = vars == null ? null : vars.get(name);
			if (val == null)
				val = def;
			if (val == null)
				throw new IllegalArgumentException("Missing var: " + name);
			return Matcher.quoteReplacement(String.valueOf(val));
		});
	}

	public static void assertValidJson(String json) {
		try {
			MAPPER.readTree(json);
		} catch (Exception e) {
			throw new IllegalArgumentException("Filled template is not valid JSON", e);
		}
	}
}