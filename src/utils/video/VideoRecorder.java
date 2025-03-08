package com.framework.utils;

import org.jcodec.api.awt.SequenceEncoder;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import javax.imageio.ImageIO;

public class VideoRecorder {

    public static void createVideoFromImages(List<String> imagePaths, String outputVideoPath) throws Exception {
        File outputFile = new File(outputVideoPath);
        SequenceEncoder encoder = new SequenceEncoder(outputFile);

        for (String imagePath : imagePaths) {
            File imgFile = new File(imagePath);
            if (imgFile.exists()) {
                BufferedImage image = ImageIO.read(imgFile);
                encoder.encodeImage(image);
            }
        }

        encoder.finish();
    }
}