package de.maikmerten.tcompressbfg;

import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import javax.imageio.ImageIO;

/**
 *
 * @author merten
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        File f = new File(args[0]);

        BufferedImage bimage = ImageIO.read(f);

        // while we don't write proper DDS files, dump data here
        FileOutputStream fos = new FileOutputStream(new File("/tmp/test.dat"));
        DataOutputStream dos = new DataOutputStream(fos);

        new Compressor().compressImage(bimage, dos);

        fos.close();



    }
}
