package de.maikmerten.tcompressbfg;

/**
 *
 * @author maik
 */
public class CompressorConfig {

    public final static int TEXTURECOLOR = 1;
    public final static int TEXTURENORMAL = 2;

    public int texturetype = TEXTURECOLOR;
    public int dither = 1;
    public int interpolatepower = 0;
    public boolean hasalpha = false;
    public boolean onlyreferencecolors = false;

}
