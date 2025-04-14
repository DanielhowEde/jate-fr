package utils.secrets;

import io.github.cdimascio.dotenv.Dotenv;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

public class SecretManagerUtil {

	private static final Dotenv dotenv = DotenvLoader.getDotenv();

    /**
     * Attempts to retrieve a secret in the following order:
     * 1. AWS Secrets Manager (if configured)
     * 2. Environment variable (System.getenv)
     * 3. .env file (via Dotenv)
     */
    public static String getSecret(String secretName, String envVarName) {
        // 1. Try AWS Secrets Manager
        try {
            SecretsManagerClient client = SecretsManagerClient.create();
            GetSecretValueRequest request = GetSecretValueRequest.builder()
                    .secretId(secretName)
                    .build();
            GetSecretValueResponse response = client.getSecretValue(request);

            String secretString = response.secretString();
            if (secretString != null && !secretString.isEmpty()) {
                System.out.println("🔐 Loaded secret from AWS Secrets Manager: " + secretName);
                return secretString;
            }
        } catch (Exception e) {
            System.out.println("⚠️ AWS Secrets Manager not used or failed for: " + secretName);
        }

        // 2. Try system environment variable
        String envValue = System.getenv(envVarName);
        if (envValue != null && !envValue.isEmpty()) {
            System.out.println("🔐 Loaded secret from environment variable: " + envVarName);
            return envValue;
        }

        // 3. Try .env file
        String dotenvValue = dotenv.get(envVarName);
        if (dotenvValue != null && !dotenvValue.isEmpty()) {
            System.out.println("🔐 Loaded secret from .env: " + envVarName);
            return dotenvValue;
        }

        throw new RuntimeException("❌ No secret found for " + secretName + " and no fallback environment variable: " + envVarName);
    }


    // 🔐 Use this for JKS password retrieval
    public static String getJksPassword() {
    	return getSecret("jks_password_secret", "JKS_PASSWORD");
        
    }
}
