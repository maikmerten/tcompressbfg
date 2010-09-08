package de.maikmerten.tcompressbfg.dds;

import de.maikmerten.tcompressbfg.ByteWriter;
import java.io.DataOutputStream;
import java.nio.charset.Charset;

/**
 *
 * @author merten
 */
public class DDSPixelformat {

    private final int DWSIZE = 32;
    private final int DWFLAGS = 0x4; // FourCC

    public boolean hasalpha = false;
    public String fourcc = "DXT1";


    public void writeBytes(DataOutputStream ds) throws Exception {

        ByteWriter.write32Little(DWSIZE, ds);

        int dwflags = DWFLAGS;
        if(hasalpha) {
            dwflags = dwflags | 0x1;
        }
        ByteWriter.write32Little(dwflags, ds);
        
        ds.write(fourcc.getBytes(Charset.forName("US-ASCII")));

        ByteWriter.write32Little(0, ds); // dwRGBBitCount, unused
        ByteWriter.write32Little(0x00FF0000, ds); // dwRBitMask
        ByteWriter.write32Little(0x0000FF00, ds); // dwGBitMask
        ByteWriter.write32Little(0x000000FF, ds); // dwBBitMask

        if(hasalpha) {
            ByteWriter.write32Little(0xFF000000, ds); // dwABitMask
        } else {
            ByteWriter.write32Little(0, ds);
        }


    }
}
