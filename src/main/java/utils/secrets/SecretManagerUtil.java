package utils.secrets;

import io.github.cdimascio.dotenv.Dotenv;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

public class SecretManagerUtil {

    public static String getSecret(String secretName, String envVarName) {
        try {
            SecretsManagerClient client = SecretsManagerClient.create();
            GetSecretValueRequest request = GetSecretValueRequest.builder().secretId(secretName).build();
            GetSecretValueResponse response = client.getSecretValue(request);
            return response.secretString();
        } catch (Exception e) {
            System.out.println("Secrets Manager not available or secret not found: " + secretName);
        }

        String envValue = System.getenv(envVarName);
        if (envValue != null) {
            return envValue;
        } 
        
        try {
        	Dotenv dotenv = Dotenv.load();
        	String password = dotenv.get("JKS_PASSWORD");
        	envValue = dotenv.get(envVarName);
        	return envValue;
        } catch (Exception e) { 
        	throw new RuntimeException("No secret found for " + secretName + " and no fallback environment variable: " + envVarName);
        }
    }

    // 🔐 Use this for JKS password retrieval
    public static String getJksPassword() {
        return getSecret("jks_password_secret", "JKS_PASSWORD");
    }
}