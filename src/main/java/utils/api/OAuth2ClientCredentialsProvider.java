package utils.api;


import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;


import java.time.Instant;
import java.util.Map;


class OAuth2ClientCredentialsProvider implements AuthProvider {
private final String tokenUrl, clientId, clientSecret, scope;
private String token; private Instant exp = Instant.EPOCH;


OAuth2ClientCredentialsProvider(String tokenUrl, String clientId, String clientSecret, String scope) {
this.tokenUrl = tokenUrl; this.clientId = clientId; this.clientSecret = clientSecret; this.scope = scope;
}


@Override public void apply(RequestSpecification req) {
if (token == null || Instant.now().isAfter(exp.minusSeconds(30))) refresh();
req.header("Authorization", "Bearer " + token);
}


private void refresh() {
var resp = RestAssured.given().relaxedHTTPSValidation()
.auth().preemptive().basic(clientId, clientSecret)
.contentType("application/x-www-form-urlencoded")
.formParams(Map.of("grant_type","client_credentials","scope", scope==null?"":scope))
.post(tokenUrl).then().extract().response();


if (resp.statusCode() >= 300) throw new RuntimeException("OAuth token failed: " + resp.statusCode() + " " + resp.asString());
token = resp.jsonPath().getString("access_token");
int ttl = resp.jsonPath().getInt("expires_in");
exp = Instant.now().plusSeconds(ttl > 0 ? ttl : 300);
}
}