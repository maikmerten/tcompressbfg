package de.maikmerten.rgb565converter;

import de.maikmerten.tcompressbfg.ByteWriter;
import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import javax.imageio.ImageIO;

/**
 *
 * @author maik
 */
public class Main {

    public static void main(String[] args) throws Exception {

        BufferedImage image = ImageIO.read(new File("/tmp/test.png"));
        BMPHeader header = new BMPHeader();
        header.width = image.getWidth();
        header.height = image.getHeight();


        DataOutputStream dos = new DataOutputStream(new FileOutputStream(new File("/tmp/test.bmp")));
        header.writeBytes(dos);
        for (int y = image.getHeight() - 1; y >= 0; --y) {
            for (int x = 0; x < image.getWidth(); ++x) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                // truncate bits
                int r2 = r >> 3;
                int g2 = g >> 2;
                int b2 = b >> 3;

                // convert back to 8 bit per component
                int r3 = (r2 * 255) / 31;
                int g3 = (g2 * 255) / 63;
                int b3 = (b2 * 255) / 31;

                // compute quantization error
                int rdiff = r - r3;
                int gdiff = g - g3;
                int bdiff = b - b3;

                // dither
                distributeError(image, x, y, rdiff, gdiff, bdiff);


                int rgb565 = (r2 << 11) | (g2 << 5) | b2;
                ByteWriter.write16Little(rgb565, dos);

            }
        }
    }

    public static void distributeError(BufferedImage img, int x, int y, int rdiff, int gdiff, int bdiff) {

        if(x + 1 < img.getWidth()) {
            int rgb = img.getRGB(x + 1, y);

            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8) & 0xFF;
            int b = (rgb) & 0xFF;

            r = Math.max(0, Math.min(255, r + rdiff));
            g = Math.max(0, Math.min(255, g + gdiff));
            b = Math.max(0, Math.min(255, b + bdiff));

            rgb = (r << 16) | (g << 8) | b;
            img.setRGB(x + 1, y, rgb);
        }

    }
}
