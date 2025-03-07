package com.framework.utils;

import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

public class SecretManagerUtil {

    public static String getSecret(String secretName, String envVarName) {
        try {
            // Try fetching secret from AWS Secrets Manager
            SecretsManagerClient client = SecretsManagerClient.create();
            GetSecretValueRequest request = GetSecretValueRequest.builder().secretId(secretName).build();
            GetSecretValueResponse response = client.getSecretValue(request);
            return response.secretString();
        } catch (Exception e) {
            System.out.println("Secrets Manager not available or secret not found: " + secretName);
        }

        // Fallback to environment variable
        String envValue = System.getenv(envVarName);
        if (envValue != null) {
            return envValue;
        }

        throw new RuntimeException("No secret found for " + secretName + " and no fallback environment variable: " + envVarName);
    }
}