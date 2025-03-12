package com.tests;

import com.framework.utils.JKSReader;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class JKSReaderTest {
    @Test
    public void testJKSReader() {
        JKSReader jksReader = new JKSReader("src/test/resources/db-credentials.jks", "changeit");
        String username = jksReader.getSecret("dbUsername", "changeit");

        assertNotNull(username, "DB username should not be null");
    }
}
✅ Ensures database credentials are securely retrieved from