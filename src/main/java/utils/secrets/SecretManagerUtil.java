package utils.secrets;

import io.github.cdimascio.dotenv.Dotenv;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import static core.TestLogger.LOGGER;

public class SecretManagerUtil {

	private static final Dotenv dotenv = DotenvLoader.getDotenv();

    /**
     * Attempts to retrieve a secret in the following order:
     * 1. AWS Secrets Manager (if configured)
     * 2. Environment variable (System.getenv)
     * 3. .env file (via Dotenv)
     */
    public static String getSecret(String secretName, String envVarName) {
        
        // 1. Try system environment variable
        String envValue = System.getenv(envVarName);
        if (envValue != null && !envValue.isEmpty()) {
            LOGGER.debug("Loaded secret from environment variable: [{}]", envVarName);
            return envValue;
        }

        // 2. Try .env file
        String dotenvValue = dotenv.get(envVarName);
        if (dotenvValue != null && !dotenvValue.isEmpty()) {
            LOGGER.debug("Loaded secret from .env file: [{}]", envVarName);
            return dotenvValue;
        }

    	// 3. Try AWS Secrets Manager
        try {
            SecretsManagerClient client = SecretsManagerClient.create();
            GetSecretValueRequest request = GetSecretValueRequest.builder()
                    .secretId(secretName)
                    .build();
            GetSecretValueResponse response = client.getSecretValue(request);

            String secretString = response.secretString();
            if (secretString != null && !secretString.isEmpty()) {
                LOGGER.debug("Loaded secret from AWS Secrets Manager.");
                return secretString;
            }
        } catch (Exception e) {
            LOGGER.warn("AWS Secrets Manager unavailable for secret lookup (env var name: [{}])", envVarName);
        }

        throw new RuntimeException("❌ No secret found for env var [" + envVarName + "]");
    }


    // 🔐 Use this for JKS password retrieval
    public static String getJksPassword() {
    	return getSecret("jks_password_secret", "JKS_PASSWORD");
        
    }
}
