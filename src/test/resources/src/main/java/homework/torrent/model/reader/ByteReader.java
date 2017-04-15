package homework.torrent.model.reader;

import org.jetbrains.annotations.NotNull;

/**
 * ObjectReader of byte.
 */
public class ByteReader extends AbstractObjectReader<Byte> {

    /**
     * ObjectReader of byte.
     */
    public ByteReader() {
        super(1);
    }

    @Override
    protected Byte getResultFromArray(@NotNull byte[] array) {
        return array[0];
    }
}
