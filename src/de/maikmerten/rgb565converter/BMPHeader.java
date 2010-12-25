package de.maikmerten.rgb565converter;

import de.maikmerten.tcompressbfg.ByteWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;

/**
 *
 * @author maik
 */
public class BMPHeader {

    public int width, height;
    public short bitsperpixel = 16;
    public int redmask = (31 << 11);
    public int greenmask = (63 << 5);
    public int bluemask = 31;

    public void writeBytes(DataOutputStream ds) throws Exception {

        // 14 bytes file header
        ds.write("BM".getBytes());
        ByteWriter.write32Little(0, ds); // file size, only matter when compressed

        ByteWriter.write16Little(0, ds); // reserved 1
        ByteWriter.write16Little(0, ds); // reserved 2

        // offset where image data begins. 14 bytes for initial header, then 40
        // for the WinNT Bitmap header, then 12 bytes for the color mask
        ByteWriter.write32Little(14 + 40 + 12, ds);

        // 40 bytes WinNT header
        ByteWriter.write32Little(40, ds); // size of this header
        ByteWriter.write32Little(width, ds); // width
        ByteWriter.write32Little(height, ds); // height
        ByteWriter.write16Little(1, ds); // planes
        ByteWriter.write16Little(bitsperpixel, ds);
        ByteWriter.write32Little(3, ds); // compression method
        ByteWriter.write32Little(width * height * (bitsperpixel / 8), ds); // size of bitmap in bytes
        ByteWriter.write32Little(100, ds); // horizontal: pixels per meter
        ByteWriter.write32Little(100, ds); // vertical: pixels per meter

        
        ByteWriter.write32Little((int) (1 << bitsperpixel), ds); // number of colors in image
        ByteWriter.write32Little((int) (1 << bitsperpixel), ds); // number of importan colors;

        // 12 bytes bitfield masks
        ByteWriter.write32Little(redmask, ds);
        ByteWriter.write32Little(greenmask, ds);
        ByteWriter.write32Little(bluemask, ds);

    }

    public static void main(String[] args) throws Exception {
        BMPHeader header = new BMPHeader();
        header.width = 256;
        header.height = 128;

        FileOutputStream fos = new FileOutputStream(new File("/tmp/test.bmp"));
        header.writeBytes(new DataOutputStream(fos));
        fos.flush();

    }
}
