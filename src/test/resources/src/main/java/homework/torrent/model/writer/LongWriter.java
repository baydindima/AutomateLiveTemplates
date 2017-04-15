package homework.torrent.model.writer;

import java.nio.ByteBuffer;

/**
 * ObjectWriter for long.
 */
public class LongWriter extends AbstractObjectWriter {

    /**
     * Create instance of ObjectWriter for long.
     *
     * @param data long value
     */
    public LongWriter(final long data) {
        super(ByteBuffer.allocate(Long.BYTES).putLong(data).array());
    }
}
