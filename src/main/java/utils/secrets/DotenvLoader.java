package utils.secrets;

import io.github.cdimascio.dotenv.Dotenv;

import java.io.File;

public class DotenvLoader {
    private static final Dotenv dotenv = loadDotenv();

    private static Dotenv loadDotenv() {
        // Try ENV variable override first
        String envDir = System.getenv("DOTENV_DIR");
        if (envDir != null && new File(envDir + "/.env").exists()) {
            System.out.println("✅ Using .env from DOTENV_DIR: " + envDir);
            return Dotenv.configure().directory(envDir).load();
        }

        // Try parent relative path
        File fromParent = new File("../jate-fr/.env");
        if (fromParent.exists()) {
            System.out.println("✅ Using fallback .env from ../jate-fr");
            return Dotenv.configure().directory("../jate-fr").load();
        }

        // Try local
        File local = new File(".env");
        if (local.exists()) {
            System.out.println("✅ Using local .env");
            return Dotenv.configure().load();
        }

        System.err.println("⚠️ .env file not found in DOTENV_DIR, ../jatefr, or current dir");
        return Dotenv.configure().ignoreIfMissing().load();
    }

    public static Dotenv getDotenv() {
        return dotenv;
    }
}

