package utils.api;

import io.restassured.specification.RequestSpecification;

public interface AuthProvider {
	void apply(RequestSpecification req);

	static AuthProvider from(ApiRegistry.ApiCfg cfg) {
		var a = cfg.auth;
		if (a == null || "none".equalsIgnoreCase(a.type))
			return req -> {
			};
		if ("api_key".equalsIgnoreCase(a.type))
			return req -> req.header(a.header, a.value);
		if ("oauth2_client_credentials".equalsIgnoreCase(a.type))
			return new OAuth2ClientCredentialsProvider(a.tokenUrl, a.clientId, a.clientSecret, a.scope);
		throw new IllegalArgumentException("Unsupported auth type: " + a.type);
	}
}