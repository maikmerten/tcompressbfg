package de.maikmerten.tcompressbfg;

import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author merten
 */
public class Compressor {

    public final static boolean DEBUG = true;
    private int blockcnt, processed;

    public void compressImage(BufferedImage bimage, DataOutputStream dos) {

        Block4x4[][] blocks = new Block4x4[(bimage.getHeight() / 4)][(bimage.getWidth() / 4)];
        Queue<Block4x4> jobs = new LinkedList<Block4x4>();

        System.out.println("Chopping up image...");
        for (int x = 0; x < bimage.getWidth(); x = x + 4) {
            for (int y = 0; y < bimage.getHeight(); y = y + 4) {
                int[] rgbdata = new int[16];

                for (int xoffset = 0; xoffset < 4; ++xoffset) {
                    for (int yoffset = 0; yoffset < 4; ++yoffset) {
                        rgbdata[(yoffset * 4) + xoffset] = bimage.getRGB(x + xoffset, y + yoffset);
                    }
                }

                Block4x4 block = new Block4x4(rgbdata);

                blocks[x / 4][y / 4] = block;
                jobs.add(block);
            }
        }

        blockcnt = jobs.size();

        List<Thread> threads = new LinkedList<Thread>();
        for (int i = 0; i < Runtime.getRuntime().availableProcessors(); ++i) {
            threads.add(new CompressThread(jobs));
        }

        System.out.println("Compressing...");
        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException ex) {
                Logger.getLogger(Compressor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        System.out.println("Writing compressed data...");
        for (int i = 0; i < blocks.length; ++i) {
            for (int j = 0; j < blocks[0].length; ++j) {
                try {
                    blocks[j][i].writeBytes(dos);

                } catch (Exception ex) {
                    Logger.getLogger(Compressor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }


        if (DEBUG) {
            for (int x = 0; x < bimage.getWidth(); x = x + 4) {
                for (int y = 0; y < bimage.getHeight(); y = y + 4) {
                    Block4x4 block = blocks[x / 4][y / 4];
                    int[] rgbdata2 = block.getRGBData();
                    for (int xoffset = 0; xoffset < 4; ++xoffset) {
                        for (int yoffset = 0; yoffset < 4; ++yoffset) {
                            bimage.setRGB(x + xoffset, y + yoffset, rgbdata2[(yoffset * 4) + xoffset]);
                        }
                    }

                }
            }

            try {
                ImageIO.write(bimage, "png", new File("/tmp/s3tc.png"));
                dos.close();
            } catch (IOException ex) {
                Logger.getLogger(Compressor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    private class CompressThread extends Thread {

        private final Queue<Block4x4> jobs;

        private CompressThread(Queue<Block4x4> jobs) {
            this.jobs = jobs;
        }

        @Override
        public void run() {
            boolean stop = false;

            while (!stop) {
                Block4x4 block = null;
                synchronized (jobs) {
                    block = jobs.poll();
                }

                if (block != null) {
                    int i;
                    synchronized (jobs) {
                        i = ++processed;
                    }
                    System.out.println(i + "/" + blockcnt);

                    block.compress(1, 0);
                } else {
                    stop = true;
                }

            }
        }
    }
}
