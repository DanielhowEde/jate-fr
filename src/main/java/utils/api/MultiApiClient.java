package utils.api;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import utils.config.ConfigReader;

import java.util.Map;

public class MultiApiClient {
	private final String baseUrl;
	private final AuthProvider auth;
	private final Map<String, String> defaultHeaders;

	public MultiApiClient(String baseUrl, AuthProvider auth, Map<String, String> defaultHeaders) {
		this.baseUrl = baseUrl;
		this.auth = auth;
		this.defaultHeaders = defaultHeaders;
	}

	private RequestSpecification base() {
		var req = RestAssured.given();
		// Only disable TLS validation when explicitly configured (e.g. self-signed certs in dev/test)
		if (ConfigReader.getBooleanProperty("api.relaxed.https", false)) {
			req = req.relaxedHTTPSValidation();
		}
		if (defaultHeaders != null)
			defaultHeaders.forEach(req::header);
		if (auth != null)
			auth.apply(req);
		return req;
	}

	public Response get(String path, Map<String, ?> query, Map<String, String> headers) {
		var r = base();
		if (query != null)
			r.queryParams(query);
		if (headers != null)
			headers.forEach(r::header);
		return r.get(baseUrl + path).then().extract().response();
	}

	public Response postJson(String path, String json, Map<String, String> headers) {
		var r = base().contentType("application/json").body(json);
		if (headers != null)
			headers.forEach(r::header);
		return r.post(baseUrl + path).then().extract().response();
	}

	public Response putJson(String path, String json, Map<String, String> headers) {
		var r = base().contentType("application/json").body(json);
		if (headers != null)
			headers.forEach(r::header);
		return r.put(baseUrl + path).then().extract().response();
	}

	public Response delete(String path, Map<String, String> headers) {
		var r = base();
		if (headers != null)
			headers.forEach(r::header);
		return r.delete(baseUrl + path).then().extract().response();
	}
}