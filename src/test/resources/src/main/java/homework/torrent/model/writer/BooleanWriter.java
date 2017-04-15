package homework.torrent.model.writer;

import java.nio.ByteBuffer;

/**
 * ObjectWriter for boolean.
 */
public class BooleanWriter extends AbstractObjectWriter {
    /**
     * Create new instance of boolean writer.
     *
     * @param value boolean value for writing
     */
    public BooleanWriter(final boolean value) {
        super(ByteBuffer.allocate(1).put((booleanToByte(value))).array());
    }

    /**
     * Transform boolean value to byte.
     *
     * @param value boolean value
     * @return byte value
     */
    private static byte booleanToByte(final boolean value) {
        if (value) {
            return (byte) 1;
        } else {
            return (byte) 0;
        }
    }
}
