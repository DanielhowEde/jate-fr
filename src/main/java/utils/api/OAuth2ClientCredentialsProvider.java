package utils.api;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;

import java.time.Instant;
import java.util.Map;

class OAuth2ClientCredentialsProvider implements AuthProvider {
    private static final int TOKEN_EXPIRY_BUFFER_SECONDS = 30;
    private static final int DEFAULT_TTL_SECONDS = 300;

    private final String tokenUrl, clientId, clientSecret, scope;
    private String token;
    private Instant exp = Instant.EPOCH;

    OAuth2ClientCredentialsProvider(String tokenUrl, String clientId, String clientSecret, String scope) {
        this.tokenUrl = tokenUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.scope = scope;
    }

    @Override
    public synchronized void apply(RequestSpecification req) {
        if (token == null || Instant.now().isAfter(exp.minusSeconds(TOKEN_EXPIRY_BUFFER_SECONDS))) {
            refresh();
        }
        req.header("Authorization", "Bearer " + token);
    }

    private void refresh() {
        var resp = RestAssured.given().relaxedHTTPSValidation()
            .auth().preemptive().basic(clientId, clientSecret)
            .contentType("application/x-www-form-urlencoded")
            .formParams(Map.of("grant_type", "client_credentials", "scope", scope == null ? "" : scope))
            .post(tokenUrl).then().extract().response();

        if (resp.statusCode() >= 300) {
            // Avoid logging the full response body as it may contain sensitive data
            throw new RuntimeException("OAuth token request failed with HTTP " + resp.statusCode());
        }

        String newToken = resp.jsonPath().getString("access_token");
        if (newToken == null || newToken.isBlank()) {
            throw new RuntimeException("OAuth response did not contain an access_token");
        }
        token = newToken;

        int ttl = resp.jsonPath().getInt("expires_in");
        exp = Instant.now().plusSeconds(ttl > 0 ? ttl : DEFAULT_TTL_SECONDS);
    }
}
