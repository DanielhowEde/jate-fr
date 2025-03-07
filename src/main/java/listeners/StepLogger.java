package listeners;

import java.util.Optional;

import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.*;
import core.Hooks;

public class StepLogger implements ConcurrentEventListener {

    private static final ThreadLocal<String> currentStepText = new ThreadLocal<>();

    public static Optional<String> getCurrentStepText() {
        return Optional.ofNullable(currentStepText.get());
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestStepStarted.class, event -> {
            if (event.getTestStep() instanceof PickleStepTestStep step) {
                String text = step.getStep().getText();
                currentStepText.set(text);
                Hooks.getScenarioContext().set("lastStepText", text); // 🆕 fallback cache
                System.out.println("📌 Tracking step: " + text);
            } else {
                currentStepText.remove();
            }
        });
    }
}