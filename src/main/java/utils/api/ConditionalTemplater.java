package utils.api;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConditionalTemplater {
	private static final Pattern IF_BLOCK = Pattern.compile("\\{#if\\s+([^}]+)}(.*?)((?:\\{#else})(.*?))?\\{#/if}",
			Pattern.DOTALL);

	public static String render(String template, Map<String, ?> vars) {
		String out = template;
		while (true) {
			Matcher m = IF_BLOCK.matcher(out);
			if (!m.find())
				break;
			String expr = m.group(1).trim();
			String thenPart = m.group(2);
			String elsePart = m.group(4);
			boolean ok = eval(expr, vars);
			String replacement = ok ? thenPart : (elsePart != null ? elsePart : "");
			out = out.substring(0, m.start()) + replacement + out.substring(m.end());
		}
// cleanup trailing commas like ,}\n or ,]\n
		out = out.replaceAll(",\\s*([}\\]])", "$1");
		return out;
	}

	private static boolean eval(String expr, Map<String, ?> vars) {
		String[] parts = splitExpr(expr);
		if (parts.length == 1) {
			Object v = vars.get(parts[0]);
			return truthy(v);
		}
		String op = parts[0];
		String key = parts[1];
		String rhs = parts.length > 2 ? unquote(parts[2]) : null;
		Object v = vars.get(key);
		return switch (op) {
		case "eq" -> Objects.toString(v, null) != null && Objects.toString(v, "").equals(rhs);
		case "ne" -> Objects.toString(v, null) == null || !Objects.toString(v, "").equals(rhs);
		case "contains" -> {
			if (v instanceof Collection<?> c)
				yield c.stream().anyMatch(x -> Objects.toString(x, "").equals(rhs));
			yield Objects.toString(v, "").contains(rhs);
		}
		default -> false;
		};
	}

	private static String[] splitExpr(String expr) {
		List<String> tokens = new ArrayList<>();
		boolean inQuote = false;
		StringBuilder cur = new StringBuilder();
		for (char ch : expr.toCharArray()) {
			if (ch == '"') {
				inQuote = !inQuote;
				cur.append(ch);
			} else if (Character.isWhitespace(ch) && !inQuote) {
				if (cur.length() > 0) {
					tokens.add(cur.toString());
					cur.setLength(0);
				}
			} else
				cur.append(ch);
		}
		if (cur.length() > 0)
			tokens.add(cur.toString());
		return tokens.toArray(String[]::new);
	}

	private static String unquote(String s) {
		if (s == null)
			return null;
		if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\""))
			return s.substring(1, s.length() - 1);
		return s;
	}

	private static boolean truthy(Object v) {
		if (v == null)
			return false;
		if (v instanceof Boolean b)
			return b;
		if (v instanceof Number n)
			return n.doubleValue() != 0.0;
		String s = v.toString().trim();
		return !s.isEmpty() && !"false".equalsIgnoreCase(s) && !"0".equals(s);
	}
}