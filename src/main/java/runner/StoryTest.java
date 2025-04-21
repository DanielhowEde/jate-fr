package runner;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.File;

public class StoryTest {

    @Test
    public void runUserCheckoutStory() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        StoryDefinition story = mapper.readValue(new File("src/test/resources/stories/user_checkout_story.json"), StoryDefinition.class);

        StoryRunner.runStory(story);
    }
}