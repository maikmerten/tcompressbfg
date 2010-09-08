package de.maikmerten.tcompressbfg;

import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author merten
 */
public class Block4x4 {

    private int[] rgbdata;
    private byte[] colorIndices = new byte[16];
    private Color16 c0, c1;
    private boolean iterative = true;
    int[] colors = new int[4];

    public Block4x4(int[] rgbdata) {
        if (rgbdata.length != 16) {
            throw new RuntimeException("expecting 16 RGB values!");
        }

        this.rgbdata = rgbdata;
    }

    public void compress(int dither, int interpolate) {
        int[] interpolatedrgb = rgbdata;
        for (int i = 0; i < interpolate; ++i) {
            interpolatedrgb = RGBUtil.interpolate(interpolatedrgb);
        }


        List<Color16> colorcandidates = new ArrayList<Color16>();
        Set<Color16> colorset = new HashSet<Color16>();
        for (int rgb : interpolatedrgb) {
            Color16 c = new Color16(rgb);
            if (!colorset.contains(c)) {
                colorcandidates.add(c);
                colorset.add(c);
            }
        }

        Color16[] refcolors = null;

        if (!iterative) {
            dither(dither, colorcandidates, colorset);
            refcolors = pickReferenceColors(colorcandidates);
        } else {
            // pick best candidates of original colors
            refcolors = pickReferenceColors(colorcandidates);

            Color16 oldbestc0 = refcolors[0];
            Color16 oldbestc1 = refcolors[1];
            // then dither and repeat
            for (int i = 0; i < 8; ++i) {
                colorset.clear();
                colorcandidates.clear();
                colorcandidates.add(refcolors[0]);
                colorcandidates.add(refcolors[1]);
                colorset.addAll(colorcandidates);
                dither(dither, colorcandidates, colorset);
                refcolors = pickReferenceColors(colorcandidates);

                if(oldbestc0.rgb == refcolors[0].rgb && oldbestc1.rgb == refcolors[1].rgb) {
                    // fixpoint reached
                    break;
                }
                oldbestc0 = refcolors[0];
                oldbestc1 = refcolors[1];
            }
        }


        this.c0 = refcolors[0];
        this.c1 = refcolors[1];
        if (c0.rgb > c1.rgb) {
            c0 = refcolors[1];
            c1 = refcolors[0];
        }

        computeColors();
        pickColorIndex();
    }

    private void dither(int dither, List<Color16> colorcandidates, Set<Color16> colorset) {
        if (dither > 0) {
            List<Color16> wigglecolors = new ArrayList<Color16>();
            for (Color16 basecolor : colorcandidates) {
                for (int roff = -dither; roff <= dither; ++roff) {
                    for (int goff = -dither; goff <= dither; ++goff) {
                        for (int boff = -dither; boff <= dither; ++boff) {
                            if ((roff | goff | roff) != 0) {
                                Color16 newcolor = new Color16(basecolor);
                                newcolor.applyOffset(roff, goff, boff);
                                wigglecolors.add(newcolor);
                            }
                        }
                    }
                }
            }

            for (Color16 c : wigglecolors) {
                if (!colorset.contains(c)) {
                    colorcandidates.add(c);
                    colorset.add(c);
                }
            }
        }

    }

    private Color16[] pickReferenceColors(List<Color16> colorcandidates) {
        double minerror = Integer.MAX_VALUE;
        Color16 bestc0 = colorcandidates.get(0);
        Color16 bestc1 = colorcandidates.get(0);

        for (int idx1 = 0; idx1 < colorcandidates.size(); ++idx1) {
            for (int idx2 = idx1 + 1; idx2 < colorcandidates.size(); ++idx2) {

                this.c0 = colorcandidates.get(idx1);
                this.c1 = colorcandidates.get(idx2);

                computeColors();
                pickColorIndex();
                double error = RGBUtil.getRGBDistanceMSE(rgbdata, getRGBData());

                if (error < minerror) {
                    minerror = error;
                    bestc0 = c0;
                    bestc1 = c1;
                }
            }
        }

        return new Color16[]{bestc0, bestc1};
    }

    private void computeColors() {

        int r0 = c0.rgb_r;
        int g0 = c0.rgb_g;
        int b0 = c0.rgb_b;

        int r1 = c1.rgb_r;
        int g1 = c1.rgb_g;
        int b1 = c1.rgb_b;

        int r2 = Math.min(Math.round((1f * (r0 + r0 + r1)) / 3f), 255);
        int r3 = Math.min(Math.round((1f * (r0 + r1 + r1)) / 3f), 255);

        int g2 = Math.min(Math.round((1f * (g0 + g0 + g1)) / 3f), 255);
        int g3 = Math.min(Math.round((1f * (g0 + g1 + g1)) / 3f), 255);

        int b2 = Math.min(Math.round((1f * (b0 + b0 + b1)) / 3f), 255);
        int b3 = Math.min(Math.round((1f * (b0 + b1 + b1)) / 3f), 255);

        this.colors[1] = c0.rgb;
        this.colors[0] = c1.rgb;
        this.colors[3] = (r2 << 16) | (g2 << 8) | b2;
        this.colors[2] = (r3 << 16) | (g3 << 8) | b3;
    }

    private void pickColorIndex() {

        for (int i = 0; i < rgbdata.length; ++i) {
            int datacolor = rgbdata[i];

            double minerror = Integer.MAX_VALUE;
            byte minidx = 0;
            for (byte idx = 0; idx < 4; ++idx) {
                int palettecolor = colors[idx];

                double error = RGBUtil.getRGBDistanceSquared(datacolor, palettecolor);
                if (error < minerror) {
                    minerror = error;
                    minidx = idx;
                }
            }

            colorIndices[i] = minidx;
        }
    }

    public final int[] getRGBData() {
        int[] result = new int[16];

        for (int i = 0; i < colorIndices.length; ++i) {
            short index = colorIndices[i];
            result[i] = colors[index];
        }

        return result;
    }

    public void writeBytes(DataOutputStream ds) throws Exception {
        int bits = 0;
        bits = c0.r << 11 | c0.g << 5 | c0.b;
        bits = bits << 16;
        bits = bits | c1.r << 11 | c1.g << 5 | c1.b;

        ByteWriter.write32Little(bits, ds);


        bits = 0;
        bits = colorIndices[0]
                | colorIndices[1] << 2
                | colorIndices[2] << 4
                | colorIndices[3] << 6
                | colorIndices[4] << 8
                | colorIndices[5] << 10
                | colorIndices[6] << 12
                | colorIndices[7] << 14
                | colorIndices[8] << 16
                | colorIndices[9] << 18
                | colorIndices[10] << 20
                | colorIndices[11] << 22
                | colorIndices[12] << 24
                | colorIndices[13] << 26
                | colorIndices[14] << 28
                | colorIndices[15] << 30;

        ByteWriter.write32Little(bits, ds);

    }
}
