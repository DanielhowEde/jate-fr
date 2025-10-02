package common.stepdefinitions;

import io.cucumber.java.en.*;
import io.restassured.response.Response;
import utils.api.*;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MultiApiSteps {
	private final Map<String, Object> ctx = new HashMap<>();

	@Given("I use the {string} API in {string} environment")
	public void i_use_api_env(String apiName, String env) {
		var cfg = ApiRegistry.get(env, apiName);
		var client = new MultiApiClient(cfg.baseUrl, AuthProvider.from(cfg), cfg.defaultHeaders);
		ctx.put("apiClient", client);
		ctx.put("apiName", apiName);
		ctx.put("apiEnv", env);
	}

	@Given("I prepare {string} template for this API with:")
	public void prepare_template_for_api(String templateName, Map<String, String> data) {
		String api = (String) ctx.get("apiName");
		String version = data.getOrDefault("_version", System.getProperty("api.version", "v1"));
		data.remove("_version");

		String raw = TemplateLoader.load("/api/" + api + "/" + version + "/templates/" + templateName + ".json");

		Map<String, Object> baseVars = (Map<String, Object>) ctx.getOrDefault("vars", new HashMap<>());
		Map<String, Object> vars = new HashMap<>(baseVars);
		vars.putAll(data);

		String withConds = ConditionalTemplater.render(raw, vars);
		String filled = TemplateLoader.render(withConds, vars, true);
		ctx.put("lastPayload", filled);
		ctx.put("lastHeaders", new HashMap<String, String>());
	}

	@And("I add request header {string} = {string}")
	public void add_header(String k, String v) {
		((Map<String, String>) ctx.get("lastHeaders")).put(k, v);
	}

	@When("I POST {string}")
	public void post_to(String path) {
		sendWithBody("POST", path);
	}

	@When("I PUT {string}")
	public void put_to(String path) {
		sendWithBody("PUT", path);
	}

	@When("I GET {string}")
	public void get_from(String path) {
		var client = (MultiApiClient) ctx.get("apiClient");
		var headers = (Map<String, String>) ctx.getOrDefault("lastHeaders", new HashMap<>());
		String dir = EvidenceWriter.saveRequest(scenarioName(), "GET", fullUrl(client, path), "", headers);
		Response resp = client.get(path, null, headers);
		cache(resp);
		EvidenceWriter.saveResponse(dir, resp);
	}

	private void sendWithBody(String method, String path) {
		var client = (MultiApiClient) ctx.get("apiClient");
		String body = (String) ctx.get("lastPayload");
		var headers = (Map<String, String>) ctx.get("lastHeaders");
		String dir = EvidenceWriter.saveRequest(scenarioName(), method, fullUrl(client, path), body, headers);
		Response resp = "POST".equals(method) ? client.postJson(path, body, headers)
				: client.putJson(path, body, headers);
		cache(resp);
		EvidenceWriter.saveResponse(dir, resp);
	}

	@Then("the response status should be {int}")
	public void status_should_be(int code) {
		assertEquals(code, (int) ctx.get("lastStatus"), "Body: " + ctx.get("lastBody"));
	}

	@And("I capture response fields:")
	public void capture_response_fields(io.cucumber.datatable.DataTable table) {
		var resp = (Response) ctx.get("lastResponse");
		Map<String, Object> vars = (Map<String, Object>) ctx.computeIfAbsent("vars", k -> new HashMap<>());
		table.asLists().forEach(row -> {
			String varName = row.get(0);
			String expr = row.get(1);
			Object val = JsonPathExtractor.extract(resp, expr);
			if (val != null)
				vars.put(varName, val);
		});
	}

	private void cache(Response r) {
		ctx.put("lastResponse", r);
		ctx.put("lastStatus", r.statusCode());
		ctx.put("lastBody", r.asString());
	}

	private String scenarioName() {
		return (String) ctx.getOrDefault("scenarioName", "Scenario");
	}

// Helper to show full URL in evidence cURL
	private String fullUrl(MultiApiClient client, String path) {
		return path;
	}
}