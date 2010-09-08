package de.maikmerten.tcompressbfg;

import de.maikmerten.tcompressbfg.dds.DDSHeader;
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
        String inputfilename = args[0];

        String outputfilename = inputfilename.substring(0, inputfilename.lastIndexOf(".")) + ".dds";
        System.out.println("Writing to " + outputfilename);

        File f = new File(inputfilename);

        BufferedImage bimage = ImageIO.read(f);

        
        FileOutputStream fos = new FileOutputStream(new File(outputfilename));
        DataOutputStream dos = new DataOutputStream(fos);


        DDSHeader header = new DDSHeader();
        header.width = bimage.getWidth();
        header.height = bimage.getHeight();
        header.writeBytes(dos);

        new Compressor().compressImage(bimage, dos);

        fos.close();



    }
}
