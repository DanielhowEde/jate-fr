package utils.api;

import io.restassured.response.Response;

import java.util.*;
import static java.util.stream.Collectors.joining;

public class JsonPathExtractor {
	public static Object extract(Response resp, String expr) {
		String path = expr;
		List<String> mods = new ArrayList<>();
		int pipe = expr.indexOf('|');
		if (pipe > -1) {
			path = expr.substring(0, pipe).trim();
			for (String m : expr.substring(pipe + 1).split("\\|"))
				mods.add(m.trim());
		}
		Object val = resp.jsonPath().get(path);
		for (String mod : mods) {
			if (val == null)
				break;
			switch (mod) {
			case "first" -> {
				if (val instanceof List<?> l)
					val = l.isEmpty() ? null : l.get(0);
			}
			case "last" -> {
				if (val instanceof List<?> l)
					val = l.isEmpty() ? null : l.get(l.size() - 1);
			}
			case "size" -> {
				if (val instanceof Collection<?> c)
					val = c.size();
				else if (val instanceof Map<?, ?> m)
					val = m.size();
				else
					val = (val == null) ? 0 : 1;
			}
			default -> {
				if (mod.startsWith("join(") && mod.endsWith(")")) {
					String sep = mod.substring(5, mod.length() - 1);
					if (val instanceof List<?> l)
						val = l.stream().map(Objects::toString).collect(joining(sep));
				}
			}
			}
		}
		return val;
	}
}