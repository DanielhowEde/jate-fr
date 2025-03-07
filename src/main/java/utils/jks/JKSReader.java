package utils.jks;

import utils.secrets.SecretManagerUtil;

import javax.crypto.SecretKey;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.Base64;

public class JKSReader {
    private KeyStore keyStore;
    private static final String JKS_PATH = "src/test/resources/credentials.jks";

    public JKSReader() {
        String jksPassword = SecretManagerUtil.getJksPassword();
        try (FileInputStream fis = new FileInputStream(JKS_PATH)) {
            keyStore = KeyStore.getInstance("JCEKS");
            keyStore.load(fis, jksPassword.toCharArray());
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to load JKS file: " + JKS_PATH, e);
        }
    }

    public String getSecret(String alias) {
        try {
            String keyPassword = SecretManagerUtil.getJksPassword(); // can be same or different from store password
            KeyStore.ProtectionParameter protParam = new KeyStore.PasswordProtection(keyPassword.toCharArray());
            KeyStore.SecretKeyEntry entry = (KeyStore.SecretKeyEntry) keyStore.getEntry(alias, protParam);
            SecretKey secretKey = entry.getSecretKey();
            return new String(secretKey.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to retrieve secret from JKS: " + alias, e);
        }
    }

    public String getSecretBase64(String alias) {
        try {
            String keyPassword = SecretManagerUtil.getJksPassword();
            KeyStore.ProtectionParameter protParam = new KeyStore.PasswordProtection(keyPassword.toCharArray());
            KeyStore.SecretKeyEntry entry = (KeyStore.SecretKeyEntry) keyStore.getEntry(alias, protParam);
            SecretKey secretKey = entry.getSecretKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to retrieve secret from JKS: " + alias, e);
        }
    }
}