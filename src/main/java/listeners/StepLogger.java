package listeners;

import java.util.Optional;

import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.*;
import core.Hooks;
import static core.TestLogger.LOGGER;

public class StepLogger implements ConcurrentEventListener {

    private static final ThreadLocal<String> currentStepText = new ThreadLocal<>();

    public static Optional<String> getCurrentStepText() {
        return Optional.ofNullable(currentStepText.get());
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestStepStarted.class, event -> {
            try {
                if (event.getTestStep() instanceof PickleStepTestStep step) {
                    String text = step.getStep().getText();
                    currentStepText.set(text);
                    Hooks.getScenarioContext().set("lastStepText", text);
                    LOGGER.debug("Tracking step: {}", text);
                } else {
                    currentStepText.remove();
                }
            } catch (Exception e) {
                // Never let listener failures interrupt test execution
                currentStepText.remove();
                LOGGER.warn("StepLogger failed to track step", e);
            }
        });

        // Clear ThreadLocal when the test step finishes to prevent leaks
        publisher.registerHandlerFor(TestStepFinished.class, event -> currentStepText.remove());
    }
}
