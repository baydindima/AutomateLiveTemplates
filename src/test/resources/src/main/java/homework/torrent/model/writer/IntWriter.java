package homework.torrent.model.writer;

import java.nio.ByteBuffer;

/**
 * ObjectWriter for integer.
 */
public class IntWriter extends AbstractObjectWriter {
    /**
     * Create new instance of int writer.
     *
     * @param value value to write
     */
    public IntWriter(final int value) {
        super(ByteBuffer.allocate(Integer.BYTES).putInt(value).array());
    }
}
