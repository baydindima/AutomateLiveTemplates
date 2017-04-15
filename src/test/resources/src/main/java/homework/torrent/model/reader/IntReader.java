package homework.torrent.model.reader;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

/**
 * ObjectReader of integer.
 */
public class IntReader extends AbstractObjectReader<Integer> {
    /**
     * Create new instance of int reader.
     */
    public IntReader() {
        super(Integer.BYTES);
    }


    /**
     * Transform raw byte array to resulting type.
     *
     * @param array raw byte array
     * @return resulting object
     */
    @Override
    protected final Integer getResultFromArray(@NotNull final byte[] array) {
        return ByteBuffer.wrap(array).getInt();
    }

}
