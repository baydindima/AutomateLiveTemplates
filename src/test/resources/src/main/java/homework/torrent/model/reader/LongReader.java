package homework.torrent.model.reader;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

/**
 * ObjectReader for longs.
 */
public class LongReader extends AbstractObjectReader<Long> {
    /**
     * Create new instance of long reader.
     */
    public LongReader() {
        super(Long.BYTES);
    }

    /**
     * Transform raw byte array to resulting type.
     *
     * @param array raw byte array
     * @return resulting object
     */
    @Override
    protected final Long getResultFromArray(@NotNull final byte[] array) {
        return ByteBuffer.wrap(array).getLong();
    }
}
