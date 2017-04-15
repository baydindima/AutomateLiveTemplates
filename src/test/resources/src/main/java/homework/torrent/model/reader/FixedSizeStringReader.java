package homework.torrent.model.reader;

import org.jetbrains.annotations.NotNull;

/**
 * ObjectReader for string, required size of string.
 */
class FixedSizeStringReader extends AbstractObjectReader<String> {

    /**
     * Create instance of string processor.
     *
     * @param size size of string
     */
    FixedSizeStringReader(final int size) {
        super(size);
    }

    /**
     * Transform raw byte array to resulting type.
     *
     * @param array raw byte array
     * @return resulting object
     */
    @Override
    protected final String getResultFromArray(@NotNull final byte[] array) {
        return new String(array);
    }
}
