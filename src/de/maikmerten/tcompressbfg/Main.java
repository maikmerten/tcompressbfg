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

    public static void main(String[] args) throws Exception {
        boolean mipmaps = true;
        boolean alphachannel = false;
        boolean normalmap = false;
        boolean onlyreferencecolors = false;

        for (int i = 0; i < args.length; ++i) {
            if (args[i].equals("-normalmap")) {
                normalmap = true;
            }
            if (args[i].equals("-onlyreferencecolors")) {
                onlyreferencecolors = true;
            }
        }

        String inputfilename = args[args.length - 1];
        String outputfilename = inputfilename.substring(0, inputfilename.lastIndexOf(".")) + ".dds";

        File f = new File(inputfilename);

        BufferedImage bimage = ImageIO.read(f);

        if (bimage.getWidth() % 4 != 0 || bimage.getHeight() % 4 != 0) {
            System.out.println("### Image dimensions are not multiples of four. Aborting. ###");
            System.exit(1);
        }


        double wlog = Math.log(bimage.getWidth()) / Math.log(2);
        double hlog = Math.log(bimage.getHeight()) / Math.log(2);
        if (Math.round(wlog) != wlog || Math.round(hlog) != hlog) {
            System.out.println("### Image dimensions are not powers of two. Disabling mipmaps. ###");
            mipmaps = false;
        }

        if (bimage.getType() == BufferedImage.TYPE_4BYTE_ABGR) {
            System.out.println("### Image has alpha channel ###");
            alphachannel = true;
        }

        List<BufferedImage> mips = new LinkedList<BufferedImage>();
        mips.add(bimage);

        if (mipmaps) {
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
        }


        FileOutputStream fos = new FileOutputStream(new File(outputfilename));
        DataOutputStream dos = new DataOutputStream(fos);


        DDSHeader header = new DDSHeader();
        header.width = bimage.getWidth();
        header.height = bimage.getHeight();

        if (alphachannel) {
            header.pixelformat.fourcc = "DXT5";
            header.pixelformat.hasalpha = true;
            header.bytesperblock = 16;
        }

        if (mips.size() > 1) {
            header.mipmaps = mips.size();
        }

        System.out.println("Writing to " + outputfilename);
        header.writeBytes(dos);

        CompressorConfig config = new CompressorConfig();
        config.hasalpha = alphachannel;
        if (normalmap) {
            config.texturetype = CompressorConfig.TEXTURENORMAL;
        }

        config.onlyreferencecolors = onlyreferencecolors;

        Compressor compressor = new Compressor();
        for (BufferedImage mip : mips) {
            compressor.compressImage(mip, dos, config);
        }

        fos.close();

    }
}
