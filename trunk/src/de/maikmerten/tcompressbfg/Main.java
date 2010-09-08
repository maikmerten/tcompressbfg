package de.maikmerten.tcompressbfg;

import de.maikmerten.tcompressbfg.dds.DDSHeader;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.LinkedList;
import java.util.List;
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



        List<BufferedImage> mips = new LinkedList<BufferedImage>();
        mips.add(bimage);
        int width = bimage.getWidth();
        int height = bimage.getHeight();

        while (width > 1 || height > 1) {
            width = width >> 1;
            height = height >> 1;

            width = Math.max(width, 1);
            height = Math.max(height, 1);

            BufferedImage mip = new BufferedImage(width, height, bimage.getType());
            Graphics2D graphics2D = mip.createGraphics();
            AffineTransform xform = AffineTransform.getScaleInstance((1d * width) / bimage.getWidth(), (1d * height) / bimage.getHeight());
            graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics2D.drawImage(bimage, xform, null);
            graphics2D.dispose();

            mips.add(mip);
        }




        FileOutputStream fos = new FileOutputStream(new File(outputfilename));
        DataOutputStream dos = new DataOutputStream(fos);


        DDSHeader header = new DDSHeader();
        header.width = bimage.getWidth();
        header.height = bimage.getHeight();

        if(mips.size() > 1) {
            header.mipmaps = mips.size();
        }

        header.writeBytes(dos);


        for (BufferedImage mip : mips) {
            new Compressor().compressImage(mip, dos);
        }

        fos.close();



    }
}
