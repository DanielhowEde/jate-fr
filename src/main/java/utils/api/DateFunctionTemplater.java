package utils.api;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateFunctionTemplater {
	private static final Pattern DATE_FUNC = Pattern.compile("\\{date\\(([^)}]+)\\),\\s*([^}]+)}");
	private static final Pattern DATE_FUNC_EX = Pattern.compile("\\{date\\(([^)}]+)}");

	private static final List<String> DEFAULT_INPUT_PATTERNS = List.of("dd/MM/yyyy HH:mm:ss", "dd/MM/yyyy HH:mm",
			"dd/MM/yyyy", "ddMMyyyyHHmmss", "ddMMyyyy", "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", "yyyy-MM-dd'T'HH:mm:ssXXX",
			"yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM-dd", "yyyyMMddHHmmss",
			"yyyyMMdd");

	private static ZoneId zone() {
		String z = System.getProperty("api.timezone", "Europe/London");
		try {
			return ZoneId.of(z);
		} catch (Exception e) {
			return ZoneId.of("Europe/London");
		}
	}

	public static String render(String template, Map<String, ?> vars) {
		if (template == null || template.indexOf('{') < 0)
			return template;
		String out = replaceBasic(template, vars);
		out = replaceExtended(out, vars);
		return out;
	}

	private static String replaceBasic(String s, Map<String, ?> vars) {
		Matcher m = DATE_FUNC.matcher(s);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			String arg = m.group(1).trim();
			String outPattern = m.group(2).trim();
			String replacement = formatArg(arg, outPattern, vars);
			m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
		}
		m.appendTail(sb);
		return sb.toString();
	}

	private static String replaceExtended(String s, Map<String, ?> vars) {
		Matcher m = DATE_FUNC_EX.matcher(s);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			String arg = m.group(1).trim();
			Map<String, String> opts = parseOpts(arg);
			String base = opts.getOrDefault("base", "now");
			String inPat = opts.get("in");
			String outPat = opts.getOrDefault("out", "yyyy-MM-dd'T'HH:mm:ss");
			String replacement = formatArgWithInOut(base, inPat, outPat, vars);
			m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
		}
		m.appendTail(sb);
		return sb.toString();
	}

	private static Map<String, String> parseOpts(String arg) {
		Map<String, String> map = new HashMap<>();
		String[] parts = arg.split("\\|");
		map.put("base", parts[0].trim());
		for (int i = 1; i < parts.length; i++) {
			int eq = parts[i].indexOf('=');
			if (eq > 0)
				map.put(parts[i].substring(0, eq).trim(), parts[i].substring(eq + 1).trim());
		}
		return map;
	}

	private static String formatArg(String base, String outPattern, Map<String, ?> vars) {
		ZonedDateTime zdt = resolveBaseDate(base, vars, null);
		return DateTimeFormatter.ofPattern(outPattern).format(zdt);
	}

	private static String formatArgWithInOut(String base, String inPattern, String outPattern, Map<String, ?> vars) {
		ZonedDateTime zdt = resolveBaseDate(base, vars, inPattern);
		return DateTimeFormatter.ofPattern(outPattern).format(zdt);
	}

	private static ZonedDateTime resolveBaseDate(String base, Map<String, ?> vars, String explicitInPattern) {
		if (base.startsWith("now")) {
			ZonedDateTime t = ZonedDateTime.now(zone());
			String off = base.substring(3);
			return applyOffset(t, off);
		}
		String name = base;
		String off = "";
		int plusMinus = indexOfPlusMinusOutsideParens(base);
		if (plusMinus > 0) {
			name = base.substring(0, plusMinus);
			off = base.substring(plusMinus);
		}
		Object val = vars == null ? null : vars.get(name);
		String text = val == null ? "" : String.valueOf(val).trim();
		if (text.isEmpty())
			throw new IllegalArgumentException("Missing date source for variable: " + name);
		ZonedDateTime parsed = parseDate(text, explicitInPattern);
		return applyOffset(parsed, off);
	}

	private static ZonedDateTime applyOffset(ZonedDateTime t, String off) {
		if (off == null || off.isBlank())
			return t;
		Matcher m = Pattern.compile("([+-])(\\d+)([yMdhm])").matcher(off);
		ZonedDateTime cur = t;
		while (m.find()) {
			int sign = m.group(1).equals("-") ? -1 : 1;
			int amt = Integer.parseInt(m.group(2)) * sign;
			switch (m.group(3)) {
			case "y" -> cur = cur.plusYears(amt);
			case "M" -> cur = cur.plusMonths(amt);
			case "d" -> cur = cur.plusDays(amt);
			case "h" -> cur = cur.plusHours(amt);
			case "m" -> cur = cur.plusMinutes(amt);
			}
		}
		return cur;
	}

	private static int indexOfPlusMinusOutsideParens(String s) {
		int depth = 0;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '(')
				depth++;
			else if (c == ')')
				depth = Math.max(0, depth - 1);
			else if ((c == '+' || c == '-') && depth == 0)
				return i;
		}
		return -1;
	}

	private static ZonedDateTime parseDate(String txt, String explicitPattern) {
		ZoneId z = zone();
		if (explicitPattern != null && !explicitPattern.isBlank()) {
			DateTimeFormatter f = DateTimeFormatter.ofPattern(explicitPattern).withZone(z);
			return parseWith(f, txt, z);
		}
		for (String p : DEFAULT_INPUT_PATTERNS) {
			try {
				DateTimeFormatter f = DateTimeFormatter.ofPattern(p).withZone(z);
				return parseWith(f, txt, z);
			} catch (Exception ignored) {
			}
		}
		try {
			return ZonedDateTime.parse(txt);
		} catch (Exception ignored) {
		}
		try {
			return LocalDateTime.parse(txt).atZone(z);
		} catch (Exception ignored) {
		}
		try {
			return LocalDate.parse(txt).atStartOfDay(z);
		} catch (Exception ignored) {
		}
		throw new IllegalArgumentException("Unrecognized date format: " + txt);
	}

	private static ZonedDateTime parseWith(DateTimeFormatter f, String txt, ZoneId z) {
		try {
			return ZonedDateTime.parse(txt, f);
		} catch (Exception ignored) {
		}
		try {
			return LocalDateTime.parse(txt, f).atZone(z);
		} catch (Exception ignored) {
		}
		return LocalDate.parse(txt, f).atStartOfDay(z);
	}

}