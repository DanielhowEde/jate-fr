package core;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestLogger {
    public static final Logger LOGGER = LoggerFactory.getLogger(TestLogger.class);

    public void doSomething() {
        LOGGER.info("📌 Starting operation...");
        LOGGER.debug("📌 Debug details here...");
        LOGGER.error("⚠️ Something went wrong!", new Exception());
    }
}