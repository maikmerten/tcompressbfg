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

    private CompressorConfig config;
    private int[] rgbdata;
    private byte[] colorIndices = new byte[16];
    private Color16 c0, c1;
    private boolean iterative = true;
    int[] colors = new int[4];
    int[] alphavalues;
    int[] decompressedRGB = new int[16];
    byte[] alphaIndices;

    public Block4x4(int[] rgbdata, CompressorConfig config) {
        if (rgbdata.length != 16) {
            throw new RuntimeException("expecting 16 RGB values!");
        }

        this.rgbdata = rgbdata;
        this.config = config;
    }

    private double computeBlockError(int[] block1, int[] block2) {
        switch (config.texturetype) {
            case (CompressorConfig.TEXTURENORMAL):
                return RGBUtil.getRGBVectorAngle(block1, block2);
            default:
                return RGBUtil.getSquaredRGBDistanceLuminance(block1, block2);
        }
    }

    private double computeTexelError(int texel1, int texel2) {
        switch (config.texturetype) {
            case (CompressorConfig.TEXTURENORMAL):
                return RGBUtil.getRGBVectorAngle(texel1, texel2);
            default:
                return RGBUtil.getRGBDistanceSquared(texel1, texel2);
        }
    }

    public void compress() {
        int[] interpolatedrgb = rgbdata;
        for (int i = 0; i < config.interpolatepower; ++i) {
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
            dither(config.dither, colorcandidates, colorset);
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
                dither(config.dither, colorcandidates, colorset);
                refcolors = pickReferenceColors(colorcandidates);

                if (oldbestc0.rgb == refcolors[0].rgb && oldbestc1.rgb == refcolors[1].rgb) {
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

        if (config.hasalpha) {
            computeAlphaValues(RGBUtil.getMinMaxAlpha(rgbdata));
            pickAlphaIndex();
        }
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
                computeRGBData();
                double error = computeBlockError(rgbdata, decompressedRGB);

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

    private void computeAlphaValues(int[] alphaminmax) {
        alphavalues = new int[8];
        alphavalues[0] = alphaminmax[1];
        alphavalues[1] = alphaminmax[0];

        for (int i = 1; i <= 6; ++i) {
            alphavalues[i + 1] = (((7 - i) * alphavalues[0]) + (i * alphavalues[1])) / 7;
        }

    }

    private void pickColorIndex() {

        for (int i = 0; i < rgbdata.length; ++i) {
            int datacolor = rgbdata[i];

            double minerror = Integer.MAX_VALUE;
            byte minidx = 0;
            for (byte idx = 0; idx < 4; ++idx) {
                int palettecolor = colors[idx];

                double error = computeTexelError(datacolor, palettecolor);
                if (error < minerror) {
                    minerror = error;
                    minidx = idx;
                }
            }

            colorIndices[i] = minidx;
        }
    }

    private void pickAlphaIndex() {
        alphaIndices = new byte[rgbdata.length];
        for (int i = 0; i < alphaIndices.length; ++i) {
            int alpha = (rgbdata[i] >> 24) & 0xFF;
            int minerror = Integer.MAX_VALUE;
            for (byte alphaidx = 0; alphaidx < alphavalues.length; ++alphaidx) {
                int error = Math.abs(alphavalues[alphaidx] - alpha);
                if (error < minerror) {
                    minerror = error;
                    alphaIndices[i] = alphaidx;
                }
            }
        }
    }

    public final void computeRGBData() {
        for (int i = 0; i < colorIndices.length; ++i) {
            short index = colorIndices[i];
            decompressedRGB[i] = colors[index];
        }
    }

    public int[] getDecompresedRGBData() {
        computeRGBData();
        if (config.texturetype == CompressorConfig.TEXTURENORMAL) {
            RGBUtil.normalizeNormals(decompressedRGB);
        }
        return decompressedRGB;
    }

    public void writeBytes(DataOutputStream ds) throws Exception {
        int bits = 0;

        if (config.hasalpha) {
            bits = alphavalues[1] << 8 | alphavalues[0];
            ByteWriter.write16Little(bits, ds);

            long lbits = 0;
            for (int i = alphaIndices.length - 1; i >= 0; --i) {
                lbits = lbits << 3;
                lbits = lbits | alphaIndices[i];
            }

            ByteWriter.write48Little(lbits, ds);
        }


        bits = c0.r << 11 | c0.g << 5 | c0.b;
        bits = bits << 16;
        bits = bits | c1.r << 11 | c1.g << 5 | c1.b;

        ByteWriter.write32Little(bits, ds);

        for (int i = colorIndices.length - 1; i >= 0; --i) {
            bits = bits << 2;
            bits = bits | colorIndices[i];
        }

        ByteWriter.write32Little(bits, ds);

    }
}
