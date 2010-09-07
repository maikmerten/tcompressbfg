package de.maikmerten.tcompressbfg;

import java.io.DataOutputStream;

/**
 *
 * @author merten
 */
public class ByteWriter {

    public static void write32Little(int bits, DataOutputStream ds) throws Exception {
        byte[] b = new byte[1];
        b[0] = (byte) (bits & 0xFF);
        ds.write(b);
        b[0] = (byte) ((bits & 0xFF00) >> 8);
        ds.write(b);
        b[0] = (byte) ((bits & 0xFF0000) >> 16);
        ds.write(b);
        b[0] = (byte) ((bits & 0xFF000000) >> 24);
        ds.write(b);

    }

}
