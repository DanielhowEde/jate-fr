package utils.api;


import io.restassured.response.Response;


import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;


public class EvidenceWriter {


private static String baseDir() {
return core.Hooks.getReportDirectory(); // adapt if needed
}


private static String ensureDir(String... parts) {
Path p = Path.of(baseDir(), parts);
try { Files.createDirectories(p); } catch (Exception ignored) {}
return p.toString();
}


private static String now() {
return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
}


public static String saveRequest(String scenarioSafeName, String method, String path,
String body, Map<String,String> headers) {
String dir = ensureDir("api", scenarioSafeName, now());
writeText(Path.of(dir, "request.json").toFile(), prettyJson(body));
writeText(Path.of(dir, "request.headers.txt").toFile(), headersToString(headers));
writeText(Path.of(dir, "request.curl.txt").toFile(), toCurl(method, path, headers, body));
return dir;
}


public static String saveResponse(String evidenceDir, Response resp) {
writeText(Path.of(evidenceDir, "response.status.txt").toFile(), String.valueOf(resp.statusCode()));
writeText(Path.of(evidenceDir, "response.headers.txt").toFile(), respHeadersToString(resp));
String body = resp.asString();
writeText(Path.of(evidenceDir, "response.json").toFile(), prettyJson(body));
return evidenceDir;
}


private static void writeText(File f, String data) {
try (FileWriter w = new FileWriter(f, false)) { w.write(data == null ? "" : data); }
catch (Exception e) { throw new RuntimeException("Failed writing: " + f, e); }
}


private static String prettyJson(String raw) {
if (raw == null || raw.isBlank()) return "";
try {
var om = new com.fasterxml.jackson.databind.ObjectMapper();
var node = om.readTree(raw);
return om.writerWithDefaultPrettyPrinter().writeValueAsString(node);
} catch (Exception e) { return raw; }
}


private static String headersToString(Map<String,String> headers) {
if (headers == null || headers.isEmpty()) return "";
StringBuilder sb = new StringBuilder();
headers.forEach((k, v) -> sb.append(k).append(": ").append(v).append("\n"));
return sb.toString();
}


private static String respHeadersToString(Response resp) {
StringBuilder sb = new StringBuilder();
resp.getHeaders().asList().forEach(h -> sb.append(h.getName()).append(": ").append(h.getValue()).append("\n"));
return sb.toString();
}


private static String toCurl(String method, String path, Map<String,String> headers, String body) {
StringBuilder sb = new StringBuilder("curl -i -X ").append(method).append(" \"")
.append(path).append("\"");
if (headers != null) headers.forEach((k,v) -> sb.append(" -H ").append(quote(k + ": " + v)));
if (body != null && !body.isBlank()) sb.append(" --data ").append(quote(body));
return sb.toString();
}


private static String quote(String s) { return "'" + s.replace("'", "'\"'\"'") + "'"; }
}