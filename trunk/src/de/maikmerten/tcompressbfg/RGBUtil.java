package de.maikmerten.tcompressbfg;

/**
 *
 * @author merten
 */
public class RGBUtil {

    public static int getRGBDistance(int rgb1, int rgb2) {
        int rdiff = ((rgb1 & 0xFF0000) >> 16) - (((rgb2 & 0xFF0000) >> 16));
        rdiff = rdiff < 0 ? -rdiff : rdiff;

        int gdiff = ((rgb1 & 0xFF00) >> 8) - (((rgb2 & 0xFF00) >> 8));
        gdiff = gdiff < 0 ? -gdiff : gdiff;

        int bdiff = (rgb1 & 0xFF) - (rgb2 & 0xFF);
        bdiff = bdiff < 0 ? -bdiff : bdiff;

        return rdiff + gdiff + bdiff;
    }

    public static int getRGBDistanceSquared(int rgb1, int rgb2) {
        int rdiff = ((rgb1 & 0xFF0000) >> 16) - (((rgb2 & 0xFF0000) >> 16));
        rdiff = rdiff < 0 ? -rdiff : rdiff;
        rdiff *= rdiff;

        int gdiff = ((rgb1 & 0xFF00) >> 8) - (((rgb2 & 0xFF00) >> 8));
        gdiff = gdiff < 0 ? -gdiff : gdiff;
        gdiff *= gdiff;

        int bdiff = (rgb1 & 0xFF) - (rgb2 & 0xFF);
        bdiff = bdiff < 0 ? -bdiff : bdiff;
        bdiff *= bdiff;

        return rdiff + gdiff + bdiff;
    }

    public static double getRGBDistanceCrossProduct(int rgb1, int rgb2) {
        int a1 = ((rgb1 & 0xFF0000) >> 16);
        int a2 = ((rgb1 & 0xFF00) >> 8);
        int a3 = rgb1 & 0xFF;


        int b1 = ((rgb2 & 0xFF0000) >> 16);
        int b2 = ((rgb2 & 0xFF00) >> 8);
        int b3 = rgb2 & 0xFF;

        int c1 = a2 * b3 - a3 * b2;
        c1 *= c1;
        int c2 = a3 * b1 - a1 * b3;
        c2 *= c2;
        int c3 = a1 * b2 - a2 * b1;
        c3 *= c3;

        return Math.sqrt(c1 + c2 + c3);
    }

    public static int getRGBDistance(int[] rgbdata1, int[] rgbdata2) {
        int distance = 0;
        for (int i = 0; i < rgbdata1.length; ++i) {
            distance += getRGBDistance(rgbdata1[i], rgbdata2[i]);
        }

        return distance;
    }

    public static int getRGBDistanceSquared(int[] rgbdata1, int[] rgbdata2) {
        int distance = 0;
        for (int i = 0; i < rgbdata1.length; ++i) {
            distance += getRGBDistanceSquared(rgbdata1[i], rgbdata2[i]);
        }

        return distance;
    }

    public static double getRGBDistanceCrossProduct(int[] rgbdata1, int[] rgbdata2) {
        double distance = 0;
        for (int i = 0; i < rgbdata1.length; ++i) {
            distance += getRGBDistanceCrossProduct(rgbdata1[i], rgbdata2[i]);
        }

        return distance;
    }

    public static double getRGBDistanceMSE(int[] rgbdata1, int[] rgbdata2) {
        int error = 0;
        for (int i = 0; i < rgbdata1.length; ++i) {
            int rgb1 = rgbdata1[i];
            int rgb2 = rgbdata2[i];
            int rgbdist = getRGBDistance(rgb1, rgb2);
            error += (rgbdist * rgbdist);
        }

        return (error * 1.0) / (3.0 * rgbdata1.length);

    }

    public static int[] interpolate(int[] rgbdata) {

        int[] result = new int[(rgbdata.length * 2) - 1];
        for (int i = 0; i < rgbdata.length - 1; ++i) {
            int rgb1 = rgbdata[i];
            int rgb2 = rgbdata[i + 1];

            int r1 = ((rgb1 & 0xFF0000) >> 16);
            int g1 = ((rgb1 & 0xFF00) >> 8);
            int b1 = rgb1 & 0xFF;

            int r2 = ((rgb2 & 0xFF0000) >> 16);
            int g2 = ((rgb2 & 0xFF00) >> 8);
            int b2 = rgb2 & 0xFF;

            int r3 = (r1 + r2) / 2;
            int g3 = (g1 + g2) / 2;
            int b3 = (b1 + b2) / 2;

            result[(i * 2)] = rgb1;
            result[(i * 2) + 1] = (r3 << 16) | (g3 << 8) | (b3);
        }
        return result;
    }

    public static int[] getMinMaxAlpha(int[] argb) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;

        for (int i = 0; i < argb.length; ++i) {
            int alpha = (argb[i] >> 24) & 0xFF;
            if (alpha < min) {
                min = alpha;
            }

            if (alpha > max) {
                max = alpha;
            }

        }


        return new int[]{min, max};
    }
}
