package tools.createjks;

import java.io.*;
import java.security.*;
import java.util.Properties;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


public class CreateJKS {
	 public static void main(String[] args) throws Exception {
	        String jksFile = "credentials.jks";
	        String csvFile = "users.csv";
	        String jksPassword = loadJksPasswordFromEnv();

	        KeyStore keyStore = KeyStore.getInstance("JCEKS");
	        File file = new File(jksFile);

	        if (file.exists()) {
	            try (FileInputStream fis = new FileInputStream(file)) {
	                keyStore.load(fis, jksPassword.toCharArray());
	            }
	        } else {
	            keyStore.load(null, null); // Create new keystore
	        }

	        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
	            String line;
	            boolean isFirst = true;

	            while ((line = reader.readLine()) != null) {
	                if (isFirst) {
	                    isFirst = false; // skip header
	                    continue;
	                }

	                String[] parts = line.split(",");
	                if (parts.length < 3) continue;

	                String alias = parts[0];
	                String username = parts[1];
	                String password = parts[2];

	                // Store as "username,password"
	                String secret = username + "," + password;
	                SecretKey key = new SecretKeySpec(secret.getBytes(), "AES");

	                KeyStore.SecretKeyEntry entry = new KeyStore.SecretKeyEntry(key);
	                KeyStore.ProtectionParameter param = new KeyStore.PasswordProtection(jksPassword.toCharArray());
	                keyStore.setEntry(alias, entry, param);
	                System.out.println("✅ Added alias: " + alias);
	            }
	        }

	        try (FileOutputStream fos = new FileOutputStream(jksFile)) {
	            keyStore.store(fos, jksPassword.toCharArray());
	        }

	        System.out.println("🔐 JKS created or updated: " + jksFile);
	    }

	    private static String loadJksPasswordFromEnv() throws IOException {
	        Properties props = new Properties();
	        props.load(new FileInputStream(".env"));
	        return props.getProperty("JKS_PASSWORD");
	    }
}
