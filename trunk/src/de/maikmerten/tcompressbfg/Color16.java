package de.maikmerten.tcompressbfg;

/**
 *
 * @author merten
 */
public class Color16 {

    private final static int RSHIFT = 16 + 3;
    private final static int GSHIFT = 8 + 2;
    private final static int BSHIFT = 0 + 3;
    private final static double MAXVAR = 31 + 63 + 31;

    short r, g, b;
    int rgb_r, rgb_g, rgb_b;
    int rgb;

    public Color16(int rgb) {
        int r_orig = (rgb & 0xFF0000) >> 16;
        int g_orig = (rgb & 0xFF00) >> 8;
        int b_orig = (rgb & 0xFF);

        r = (short) Math.min(Math.round((1f * r_orig) / 8), 31);
        g = (short) Math.min(Math.round((1f * g_orig) / 4), 63);
        b = (short) Math.min(Math.round((1f * b_orig) / 8), 31);

        computeRGB();
    }

    public Color16(Color16 color) {
        this.r = color.r;
        this.g = color.g;
        this.b = color.b;
        computeRGB();
    }

    public Color16(short r, short g, short b) {
        r = r < 0 ? 0 : r;
        r = r > 31 ? 31 : r;


        g = g < 0 ? 0 : g;
        g = g > 63 ? 63 : g;


        b = b < 0 ? 0 : b;
        b = b > 31 ? 31 : b;

        this.r = r;
        this.g = g;
        this.b = b;

        computeRGB();
    }

    private void computeRGB() {
        rgb_r = r << 3;
        rgb_g = g << 2;
        rgb_b = b << 3;
        rgb = r << RSHIFT | g << GSHIFT | b << BSHIFT;
    }

    public void applyOffset(int ro, int go, int bo) {
        r += ro;
        r = r < 0 ? 0 : r;
        r = r > 31 ? 31 : r;


        g += go;
        g = g < 0 ? 0 : g;
        g = g > 63 ? 63 : g;


        b += bo;
        b = b < 0 ? 0 : b;
        b = b > 31 ? 31 : b;

        computeRGB();
    }

    public double getVariance(Color16 color) {
        int rdiff = r - color.r;
        rdiff = rdiff < 0 ? -rdiff : rdiff;
        int gdiff = g - color.g;
        gdiff = gdiff < 0 ? -gdiff : gdiff;
        int bdiff = b - color.b;
        bdiff = bdiff < 0 ? -bdiff : bdiff;

        double sum = rdiff + gdiff + bdiff;
        return sum / MAXVAR;


    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Color16)) {
            return false;
        }

        Color16 c = (Color16) o;

        return rgb == c.rgb;
    }

    @Override
    public int hashCode() {
        return rgb;
    }
}
