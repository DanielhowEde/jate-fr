package com.tests;

import com.framework.utils.VideoRecorder;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class VideoRecorderTest {
    @Test
    public void testVideoGeneration() {
        assertDoesNotThrow(() -> VideoRecorder.createVideoFromImages(
                Arrays.asList("screenshots/test_1.png", "screenshots/test_2.png"),
                "videos/test_output.mp4"
        ));
    }
}