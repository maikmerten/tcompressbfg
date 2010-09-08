package de.maikmerten.tcompressbfg.dds;

import de.maikmerten.tcompressbfg.ByteWriter;
import java.io.DataOutputStream;

/**
 *
 * @author merten
 */
public class DDSHeader {

    private final int MAGIC = 0x20534444; // "DDS "
    private final int DWSIZE = 124;
    private final int DWFLAGS = 0x1 | 0x2 | 0x4 | 0x1000;
    private final int DWSURFACEFLAGS = 0x1000;

    public int mipmaps, width, height;
    public int bytesperblock = 8;

    public DDSPixelformat pixelformat = new DDSPixelformat();

    public void writeBytes(DataOutputStream ds) throws Exception {
        ByteWriter.write32Little(MAGIC, ds);
        ByteWriter.write32Little(DWSIZE, ds);

        int flags = DWFLAGS;
        if(mipmaps > 0) {
            flags = flags | 0x20000;
        }
        ByteWriter.write32Little(flags, ds);
        ByteWriter.write32Little(height, ds);
        ByteWriter.write32Little(width, ds);

        int bytecnt = (width / 4) * (height / 4) * bytesperblock;
        ByteWriter.write32Little(bytecnt, ds);

        
        ByteWriter.write32Little(0, ds); // dwDepth, unused
        ByteWriter.write32Little(mipmaps, ds);

        // dwReserved1[11], unused
        for(int i = 0; i < 11; ++i) {
            ByteWriter.write32Little(0, ds);
        }

        pixelformat.writeBytes(ds);

        int surfaceflags = DWSURFACEFLAGS;
        if(mipmaps > 0) {
            surfaceflags = surfaceflags | 0x8 | 0x400000;
        }
        ByteWriter.write32Little(surfaceflags, ds);

        ByteWriter.write32Little(0, ds); // dwCubemapFlags, unused

        // dwReserved2[3], unused
        for(int i = 0; i < 3; ++i) {
            ByteWriter.write32Little(0, ds);
        }

    }

}
