package utils.video;

import org.jcodec.api.SequenceEncoder;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Rational;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.channels.SeekableByteChannel;
import java.util.List;
import javax.imageio.ImageIO;

public class VideoRecorder {
	public static void createVideoFromImages(List<String> imagePaths, String outputVideoPath) throws Exception {
        File outputFile = new File(outputVideoPath);

        //Cast FileChannelWrapper to SeekableByteChannel
        SeekableByteChannel outputChannel = (SeekableByteChannel) NIOUtils.writableChannel(outputFile);

        //Use correct factory method with SeekableByteChannel and Rational FPS
        SequenceEncoder encoder = SequenceEncoder.createWithFps((org.jcodec.common.io.SeekableByteChannel) outputChannel, Rational.R(30, 1));

        for (String imagePath : imagePaths) {
            File imgFile = new File(imagePath);
            if (imgFile.exists()) {
                BufferedImage image = ImageIO.read(imgFile);

                // FIX: Convert BufferedImage to JCodec Picture format
                Picture picture = AWTUtil.fromBufferedImageRGB(image);

                // FIX: Use encodeNativeFrame() instead of encodeImage()
                encoder.encodeNativeFrame(picture);
            }
        }

        encoder.finish();
        outputChannel.close(); // Close channel after writing
    }
}
