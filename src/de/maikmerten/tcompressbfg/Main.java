/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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

        Block4x4[][] blocks = new Block4x4[(bimage.getHeight() / 4)][(bimage.getWidth() / 4)];

        for (int x = 0; x < bimage.getWidth(); x = x + 4) {
            for (int y = 0; y < bimage.getHeight(); y = y + 4) {
                System.out.println(x + ":" + y);

                int[] rgbdata = new int[16];

                for (int xoffset = 0; xoffset < 4; ++xoffset) {
                    for (int yoffset = 0; yoffset < 4; ++yoffset) {
                        rgbdata[(yoffset * 4) + xoffset] = bimage.getRGB(x + xoffset, y + yoffset);
                    }
                }

                Block4x4 block = new Block4x4(rgbdata);

                int[] rgbdata2 = block.getRGBData();
                for (int xoffset = 0; xoffset < 4; ++xoffset) {
                    for (int yoffset = 0; yoffset < 4; ++yoffset) {
                        bimage.setRGB(x + xoffset, y + yoffset, rgbdata2[(yoffset * 4) + xoffset]);
                    }
                }

                blocks[x / 4][y / 4] = block;
            }
        }

        FileOutputStream fos = new FileOutputStream("/tmp/test.dat");
        DataOutputStream dos = new DataOutputStream(fos);

        for (int i = 0; i < blocks.length; ++i) {
            for (int j = 0; j < blocks[0].length; ++j) {
                blocks[j][i].writeBytes(dos);
            }
        }




        ImageIO.write(bimage, "png", new File("/tmp/s3tc.png"));
        dos.close();




    }
}
