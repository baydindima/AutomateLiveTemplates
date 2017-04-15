package homework.torrent.model.reader;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

/**
 * ObjectReader of boolean.
 */
public class BooleanReader extends AbstractObjectReader<Boolean> {
    /**
     * Size of boolean in bytes.
     */
    private static final int BYTE_PER_BOOL = 1;

    /**
     * Create new instance of boolean reader.
     */
    public BooleanReader() {
        super(BYTE_PER_BOOL);
    }

    /**
     * Transform raw byte array to resulting type.
     *
     * @param array raw byte array
     * @return resulting object
     */
    @Override
    protected final Boolean getResultFromArray(@NotNull final byte[] array) {
        byte b = ByteBuffer.wrap(array).get();
        return b == (byte) 1;
    }
}
