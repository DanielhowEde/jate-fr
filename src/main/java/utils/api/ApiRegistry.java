package utils.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.InputStream;
import java.util.Map;
import java.util.regex.Pattern;

public class ApiRegistry {
	public static class AuthCfg {
		public String type; // none | api_key | oauth2_client_credentials
		public String tokenUrl; // oauth
		public String clientId; // oauth
		public String clientSecret; // oauth
		public String scope; // oauth
		public String header; // api_key header name
		public String value; // api_key value (supports ${ENV})
	}

	public static class ApiCfg {
		public String baseUrl;
		public AuthCfg auth;
		public Map<String, String> defaultHeaders;
	}

	static class Root {
		public Map<String, Map<String, ApiCfg>> envs;
	}

	private static final Root ROOT = load();

	private static Root load() {
		try (InputStream in = ApiRegistry.class.getResourceAsStream("/api/apis.yaml")) {
			ObjectMapper om = new ObjectMapper(new YAMLFactory());
			Root r = om.readValue(in, Root.class);
// Expand ${ENV} in strings
			r.envs.values().forEach(env -> env.values().forEach(ApiRegistry::expand));
			return r;
		} catch (Exception e) {
			throw new RuntimeException("Failed to load /api/apis.yaml", e);
		}
	}

	private static void expand(ApiCfg cfg) {
		if (cfg.baseUrl != null)
			cfg.baseUrl = exp(cfg.baseUrl);
		if (cfg.defaultHeaders != null)
			cfg.defaultHeaders.replaceAll((k, v) -> exp(v));
		if (cfg.auth != null) {
			cfg.auth.tokenUrl = exp(cfg.auth.tokenUrl);
			cfg.auth.clientId = exp(cfg.auth.clientId);
			cfg.auth.clientSecret = exp(cfg.auth.clientSecret);
			cfg.auth.scope = exp(cfg.auth.scope);
			cfg.auth.header = exp(cfg.auth.header);
			cfg.auth.value = exp(cfg.auth.value);
		}
	}

	private static String exp(String s) {
		if (s == null)
			return null;
		return Pattern.compile("\\$\\{([A-Z0-9_]+)}")
			       .matcher(s)
			       .replaceAll(mr -> System.getenv().getOrDefault(mr.group(1), ""));
	}

	public static ApiCfg get(String env, String api) {
		var envMap = ROOT.envs.get(env);
		if (envMap == null)
			throw new IllegalArgumentException("Unknown env: " + env);
		var cfg = envMap.get(api);
		if (cfg == null)
			throw new IllegalArgumentException("Unknown API in env " + env + ": " + api);
		return cfg;
	}
}