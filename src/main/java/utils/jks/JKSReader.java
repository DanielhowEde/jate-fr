package utils.jks;

import utils.secrets.SecretManagerUtil;

import javax.crypto.SecretKey;
import java.security.KeyStore;
import java.util.Base64;

public class JKSReader {
    private KeyStore keyStore;
    private static final String JKS_RESOURCE = "credentials.jks";
    private final String jksPassword = SecretManagerUtil.getJksPassword(); // ✅ CACHED HERE

    public JKSReader() {
        try (var input = Thread.currentThread().getContextClassLoader().getResourceAsStream(JKS_RESOURCE)) {
            if (input == null) {
                throw new RuntimeException("❌ credentials.jks not found in classpath.");
            }
            keyStore = KeyStore.getInstance("JCEKS");
            keyStore.load(input, jksPassword.toCharArray());
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to load JKS file: " + JKS_RESOURCE, e);
        }
    }

    public String getSecret(String alias) {
        try {
            KeyStore.ProtectionParameter protParam = new KeyStore.PasswordProtection(jksPassword.toCharArray());
            KeyStore.SecretKeyEntry entry = (KeyStore.SecretKeyEntry) keyStore.getEntry(alias, protParam);
            SecretKey secretKey = entry.getSecretKey();
            return new String(secretKey.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to retrieve secret from JKS: " + alias, e);
        }
    }

    public String getSecretBase64(String alias) {
        try {
            KeyStore.ProtectionParameter protParam = new KeyStore.PasswordProtection(jksPassword.toCharArray());
            KeyStore.SecretKeyEntry entry = (KeyStore.SecretKeyEntry) keyStore.getEntry(alias, protParam);
            SecretKey secretKey = entry.getSecretKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to retrieve secret from JKS: " + alias, e);
        }
    }
}
