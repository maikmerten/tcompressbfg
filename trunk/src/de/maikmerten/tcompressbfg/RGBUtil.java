package de.maikmerten.tcompressbfg;

/**
 *
 * @author merten
 */
public class RGBUtil {

    private static double[] convertRGBtoHSI(int rgb) {
        double r = ((double) ((rgb >> 16) & 0xFF)) / 255d;
        double g = ((double) ((rgb >> 8) & 0xFF)) / 255d;
        double b = ((double) (rgb & 0xFF)) / 255d;

        double max = Math.max(r, Math.max(g, b));
        double min = Math.min(r, Math.min(g, b));
        double c = max - min;

        double h = 0;
        double s = 0;
        double i = 0;

        if (c == 0) {
        } else if (max == r) {
            h = ((g - b) / c) % 6d;
        } else if (max == g) {
            h = ((b - r) / c) + 2;
        } else if (max == b) {
            h = ((r - g) / c) + 4;
        }

        h /= 6d;
        i = (r * 0.3) + (g * 0.59) + (b * 0.11);

        if (c == 0) {
        } else {
            s = 1d - (min / i);
        }

        return new double[]{h, s, i};
    }

    public static double getHSIDistance(int rgb1, int rgb2) {

        double[] hsivals = convertRGBtoHSI(rgb1);
        double h1 = hsivals[0];
        double s1 = hsivals[1];
        double i1 = hsivals[2];

        hsivals = convertRGBtoHSI(rgb2);
        double h2 = hsivals[0];
        double s2 = hsivals[1];
        double i2 = hsivals[2];

        double hdiff = h1 - h2;
        hdiff = hdiff < 0 ? h2 - h1 : hdiff;
        double hq = 1d - hdiff;

        double sdiff = s1 - s2;
        sdiff = sdiff < 0 ? s2 - s1 : sdiff;
        double sq = 1d - sdiff;


        double idiff = i1 - i2;
        idiff = idiff < 0 ? i2 - i1 : idiff;
        double iq = 1d - idiff;


        return -((hq * 50) + (sq * 100) + (iq * 1000));
    }

    public static double getHSIDistance(int[] rgbdata1, int[] rgbdata2) {
        double distance = 0;
        for (int i = 0; i < rgbdata1.length; ++i) {
            distance += getHSIDistance(rgbdata1[i], rgbdata2[i]);
        }
        return distance;
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
        //double acos = 1d - dotprod;
        double acos = Math.sqrt(2d * (1d - dotprod));

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
