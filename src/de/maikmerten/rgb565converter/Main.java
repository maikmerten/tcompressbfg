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

    private static int rdiff, gdiff, bdiff;

    public static void main(String[] args) throws Exception {
        File infile = new File("/tmp/test.png");
        File outfile = new File("/tmp/test.bmp");

        writeBMP(infile, outfile, 1, 1, 1);
    }

    public static void writeBMP(File input, File output, int redbits, int greenbits, int bluebits) throws Exception {
        BufferedImage image = ImageIO.read(new File("/tmp/test.png"));
        BMPHeader header = new BMPHeader();
        header.width = image.getWidth();
        header.height = image.getHeight();
        header.computeBitmasks(redbits, greenbits, bluebits);

        int rmaxvalue = maxValue(redbits);
        int gmaxvalue = maxValue(greenbits);
        int bmaxvalue = maxValue(bluebits);


        DataOutputStream dos = new DataOutputStream(new FileOutputStream(new File("/tmp/test.bmp")));
        header.writeBytes(dos);
        for (int y = image.getHeight() - 1; y >= 0; --y) {
            for (int x = 0; x < image.getWidth(); ++x) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                // truncate bits
                int r2 = r >> (8 - redbits);
                int g2 = g >> (8 - greenbits);
                int b2 = b >> (8 - bluebits);

                // convert back to 8 bit per component
                int r3 = (r2 * 255) / rmaxvalue;
                int g3 = (g2 * 255) / gmaxvalue;
                int b3 = (b2 * 255) / bmaxvalue;

                // compute quantization error
                rdiff += r - r3;
                gdiff += g - g3;
                bdiff += b - b3;

                // dither
                distributeError(image, x, y);


                int newrgb = (r2 << (greenbits + bluebits)) | (g2 << bluebits) | b2;
                ByteWriter.write16Little(newrgb, dos);

            }
        }

    }

    private static int maxValue(int bits) {
        int base = 0;
        for (int i = 0; i < bits; ++i) {
            base <<= 1;
            base |= 1;
        }
        return base;
    }

    private static void distributeError(BufferedImage img, int x, int y) {

        if (x + 1 < img.getWidth()) {
            int rgb = img.getRGB(x + 1, y);

            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8) & 0xFF;
            int b = (rgb) & 0xFF;

            int rtarget = r + rdiff;
            int gtarget = g + gdiff;
            int btarget = b + bdiff;

            r = Math.max(0, Math.min(255, rtarget));
            g = Math.max(0, Math.min(255, gtarget));
            b = Math.max(0, Math.min(255, btarget));

            // the pixel may not have been able to absorb all errors
            // (if, e.g., the value was already of 255), so compute
            // how far off we are and keep the remaining error
            rdiff = rtarget - r;
            gdiff = gtarget - g;
            bdiff = btarget - b;

            rgb = (r << 16) | (g << 8) | b;
            img.setRGB(x + 1, y, rgb);
        }

    }
}
