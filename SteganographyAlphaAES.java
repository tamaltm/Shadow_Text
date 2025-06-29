import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class SteganographyAlphaAES {

    public static void encode(String inputImagePath, String outputImagePath, String encryptedMessage) throws IOException {
        BufferedImage image = ImageIO.read(new File(inputImagePath));
        byte[] messageBytes = encryptedMessage.getBytes();
        int messageLength = messageBytes.length;

        if (messageLength * 8 + 32 > image.getWidth() * image.getHeight()) {
            throw new IllegalArgumentException("Message is too large to hide in this image.");
        }

        int width = image.getWidth();
        int height = image.getHeight();
        int msgIndex = 0;
        int bitIndex = 0;

        // Save message length in first 32 alpha bits
        for (int i = 0; i < 32; i++) {
            int x = i % width;
            int y = i / width;

            int rgb = image.getRGB(x, y);
            int alpha = (rgb >> 24) & 0xFF;

            int bit = (messageLength >> (31 - i)) & 1;
            alpha = (alpha & 0xFE) | bit;

            int newRgb = (alpha << 24) | (rgb & 0x00FFFFFF);
            image.setRGB(x, y, newRgb);
        }

        // Save encrypted message in alpha channel
        outer:
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (y * width + x < 32) continue;

                if (msgIndex >= messageBytes.length) break outer;

                int rgb = image.getRGB(x, y);
                int alpha = (rgb >> 24) & 0xFF;

                int bit = (messageBytes[msgIndex] >> (7 - bitIndex)) & 1;
                alpha = (alpha & 0xFE) | bit;

                int newRgb = (alpha << 24) | (rgb & 0x00FFFFFF);
                image.setRGB(x, y, newRgb);

                bitIndex++;
                if (bitIndex == 8) {
                    bitIndex = 0;
                    msgIndex++;
                }
            }
        }

        ImageIO.write(image, "png", new File(outputImagePath));
        System.out.println("Message encoded successfully into alpha channel!");
    }

    public static String decode(String imagePath) throws IOException {
        BufferedImage image = ImageIO.read(new File(imagePath));
        int width = image.getWidth();
        int height = image.getHeight();
        int messageLength = 0;

        // Read message length from first 32 alpha bits
        for (int i = 0; i < 32; i++) {
            int x = i % width;
            int y = i / width;

            int rgb = image.getRGB(x, y);
            int alpha = (rgb >> 24) & 0xFF;

            int bit = alpha & 1;
            messageLength = (messageLength << 1) | bit;
        }

        byte[] messageBytes = new byte[messageLength];
        int msgIndex = 0;
        int bitIndex = 0;

        outer:
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (y * width + x < 32) continue;

                if (msgIndex >= messageBytes.length) break outer;

                int rgb = image.getRGB(x, y);
                int alpha = (rgb >> 24) & 0xFF;

                int bit = alpha & 1;

                messageBytes[msgIndex] = (byte) ((messageBytes[msgIndex] << 1) | bit);
                bitIndex++;

                if (bitIndex == 8) {
                    bitIndex = 0;
                    msgIndex++;
                }
            }
        }

        return new String(messageBytes);
    }
}
