package homework.torrent.model.reader;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;

/**
 * ObjectReader for string with size in first 4 bytes.
 */
public class StringReader implements ObjectReader<String> {
    /**
     * Reader of size of string.
     */
    @NotNull
    private final IntReader sizeProcessor = new IntReader();
    /**
     * Reader of raw string.
     */
    @Nullable
    private FixedSizeStringReader fixedSizeStringProcessor;

    /**
     * Add data to processing object.
     *
     * @param byteBuffer buffer which contains data
     * @return count of read bytes
     */
    @Override
    public final int read(@NotNull final ByteBuffer byteBuffer) {
        int result = 0;
        if (fixedSizeStringProcessor == null) {
            result += sizeProcessor.read(byteBuffer);
            if (sizeProcessor.isReady()) {
                fixedSizeStringProcessor = new FixedSizeStringReader(
                        sizeProcessor.getResult());
            } else {
                return result;
            }
        }
        return result + fixedSizeStringProcessor.read(byteBuffer);
    }

    /**
     * Reader is ready to get result.
     *
     * @return true if ready, false otherwise
     */
    @Override
    public final boolean isReady() {
        return sizeProcessor.isReady()
                && (
                fixedSizeStringProcessor != null
                        && fixedSizeStringProcessor.isReady()
        );
    }

    /**
     * Return reader result.
     *
     * @return completed object
     * @throws InvalidProcessorStateException if processor not ready
     *                                        for creation of object
     */
    @NotNull
    @Override
    public final String getResult() {
        return fixedSizeStringProcessor.getResult();
    }
}
