package fr.tduf.libunlimited.framework.io;

import java.io.ByteArrayInputStream;

/**
 * Same as java.io.ByteArrayInputStream but with advanced features:
 * - ability of getting current position in the stream
 */
public class XByteArrayInputStream extends ByteArrayInputStream {
    /**
     * @see java.io.ByteArrayInputStream(byte[])
     */
    public XByteArrayInputStream(byte[] buf) {
        super(buf);
    }

    /**
     * @see java.io.ByteArrayInputStream(byte[], int, int)
     */
    public XByteArrayInputStream(byte[] buf, int offset, int length) {
        super(buf, offset, length);
    }

    /**
     *
     * @return current position in the stream
     */
    public int position() {
        return pos;
    }

    // TODO implement seek: https://stackoverflow.com/questions/3792747/seeking-a-bytearrayinputstream-using-java-io
}
