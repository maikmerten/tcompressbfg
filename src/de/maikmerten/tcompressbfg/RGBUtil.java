package de.maikmerten.tcompressbfg;

/**
 *
 * @author merten
 */
public class RGBUtil {

    private static double[] acostable = new double[20001];
    static {
        for(int i = 0; i <= 20000; ++i) {
            double val = ((i * 1d) / 10000d) -1d;
            acostable[i] = Math.acos(val);
        }
    }

    public static int getRGBDistance(int rgb1, int rgb2) {
        int rdiff = ((rgb1 & 0xFF0000) >> 16) - (((rgb2 & 0xFF0000) >> 16));
        rdiff = rdiff < 0 ? -rdiff : rdiff;

        int gdiff = ((rgb1 & 0xFF00) >> 8) - (((rgb2 & 0xFF00) >> 8));
        gdiff = gdiff < 0 ? -gdiff : gdiff;

        int bdiff = (rgb1 & 0xFF) - (rgb2 & 0xFF);
        bdiff = bdiff < 0 ? -bdiff : bdiff;

        return rdiff + gdiff + bdiff;
    }

    public static int getRGBDistance(int[] rgbdata1, int[] rgbdata2) {
        int distance = 0;
        for (int i = 0; i < rgbdata1.length; ++i) {
            distance += getRGBDistance(rgbdata1[i], rgbdata2[i]);
        }

        return distance;
    }

    public static int getRGBDistanceLuminance(int rgb1, int rgb2) {
        int r1 = (rgb1 >> 16) & 0xFF;
        int g1 = (rgb1 >> 8) & 0xFF;
        int b1 = rgb1 & 0xFF;

        int r2 = (rgb2 >> 16) & 0xFF;
        int g2 = (rgb2 >> 8) & 0xFF;
        int b2 = rgb2 & 0xFF;

        int rdiff = r1 - r2;
        rdiff = rdiff < 0 ? -rdiff : rdiff;

        int gdiff = g1 - g2;
        gdiff = gdiff < 0 ? -gdiff : gdiff;

        int bdiff = b1 - b2;
        bdiff = bdiff < 0 ? -bdiff : bdiff;

        int l1 = r1 + g1 + b1;
        int l2 = r2 + g2 + b2;

        int ldiff = l1 - l2;
        ldiff = ldiff < 0 ? -ldiff : ldiff;

        return rdiff + gdiff + bdiff + ldiff;
    }

    public static double getSquaredRGBDistanceLuminance(int[] rgbdata1, int[] rgbdata2) {
        double error = 0;
        for (int i = 0; i < rgbdata1.length; ++i) {
            int rgb1 = rgbdata1[i];
            int rgb2 = rgbdata2[i];
            double rgbdist = getRGBDistanceLuminance(rgb1, rgb2);
            error += (rgbdist * rgbdist);
        }

        return error;
    }

    public static int getRGBDistanceSquared(int rgb1, int rgb2) {
        int rdiff = ((rgb1 & 0xFF0000) >> 16) - (((rgb2 & 0xFF0000) >> 16));
        rdiff *= rdiff;

        int gdiff = ((rgb1 & 0xFF00) >> 8) - (((rgb2 & 0xFF00) >> 8));
        gdiff *= gdiff;

        int bdiff = (rgb1 & 0xFF) - (rgb2 & 0xFF);
        bdiff *= bdiff;

        return rdiff + gdiff + bdiff;
    }

    public static int getRGBDistanceSquared(int[] rgbdata1, int[] rgbdata2) {
        int distance = 0;
        for (int i = 0; i < rgbdata1.length; ++i) {
            distance += getRGBDistanceSquared(rgbdata1[i], rgbdata2[i]);
        }

        return distance;
    }

    public static double getRGBVectorAngle(int rgb1, int rgb2) {
        double r1 = ((rgb1 >> 16) & 0xFF) / 255d;
        double g1 = ((rgb1 >> 8) & 0xFF) / 255d;
        double b1 = (rgb1 & 0xFF) / 255d;

        double len1 = Math.sqrt((r1 * r1) + (g1 * g1) + (b1 * b1));

        r1 = r1 / len1;
        g1 = g1 / len1;
        b1 = b1 / len1;

        double r2 = ((rgb2 >> 16) & 0xFF) / 255d;
        double g2 = ((rgb2 >> 8) & 0xFF) / 255d;
        double b2 = (rgb2 & 0xFF) / 255d;

        double len2 = Math.sqrt((r2 * r2) + (g2 * g2) + (b2 * b2));

        r2 = r2 / len2;
        g2 = g2 / len2;
        b2 = b2 / len2;

        double dotprod = (r1 * r2) + (g1 * g2) + (b1 * b2);

        //double acos = Math.acos(dotprod);
        double acos = acostable[Math.round((float)(dotprod * 10000d)) + 10000];

        return acos;

    }

    public static double getRGBVectorAngle(int[] rgb1, int[] rgb2) {
        double error = 0;
        for (int i = 0; i < rgb1.length; ++i) {
            double angle = getRGBVectorAngle(rgb1[i], rgb2[i]);
            error += (angle * angle);
        }

        return error;
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
