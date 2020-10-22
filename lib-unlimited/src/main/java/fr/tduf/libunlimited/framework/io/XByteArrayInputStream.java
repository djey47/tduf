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
     * @return current position in the stream
     */
    public int position() {
        return pos;
    }

    /**
     * Changes current position in the stream
     * @param position  : position in bytes
     * @throws IllegalArgumentException when provided position is not reachable
     */
    public void seek(int position) throws IllegalArgumentException {
        if (position < 0 || position >= buf.length) {
            String message = String.format("Seeking to invalid position (%d). Valid positions are 0..%d", position, buf.length - 1);
            throw new IllegalArgumentException(message);
        }

        pos = position;
    }
}
