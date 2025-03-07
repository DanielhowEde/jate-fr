package com.framework.utils.jks;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class JKSReader {
    private KeyStore keyStore;
    private static final String JKS_PATH = "src/test/resources/db-credentials.jks";

    public JKSReader(String jksPassword) {
        try (FileInputStream fis = new FileInputStream(JKS_PATH)) {
            keyStore = KeyStore.getInstance("JCEKS"); // Use JCEKS for password storage
            keyStore.load(fis, jksPassword.toCharArray());
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to load JKS file: " + JKS_PATH, e);
        }
    }

    public String getSecret(String alias, String keyPassword) {
        try {
            KeyStore.ProtectionParameter protParam = new KeyStore.PasswordProtection(keyPassword.toCharArray());
            KeyStore.SecretKeyEntry entry = (KeyStore.SecretKeyEntry) keyStore.getEntry(alias, protParam);
            SecretKey secretKey = entry.getSecretKey();
            return new String(secretKey.getEncoded()); // Convert to string
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to retrieve secret from JKS: " + alias, e);
        }
    }

    public String getSecretBase64(String alias, String keyPassword) {
        try {
            KeyStore.ProtectionParameter protParam = new KeyStore.PasswordProtection(keyPassword.toCharArray());
            KeyStore.SecretKeyEntry entry = (KeyStore.SecretKeyEntry) keyStore.getEntry(alias, protParam);
            SecretKey secretKey = entry.getSecretKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded()); // Encode as Base64
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to retrieve secret from JKS: " + alias, e);
        }
    }
}