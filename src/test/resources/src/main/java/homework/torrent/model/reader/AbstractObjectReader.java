package homework.torrent.model.reader;

import homework.torrent.exception.InvalidProcessorStateException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;

/**
 * Abstract reader, for objects with known size.
 *
 * @param <T> result type
 */
abstract class AbstractObjectReader<T> implements ObjectReader<T> {
    /**
     * Current read length.
     */
    private int curLength = 0;
    /**
     * Read data.
     */
    @Nullable
    private byte[] buffer;

    /** Resulting object. */
    @Nullable
    private T result;

    /**
     * Create new instance of object reader.
     *
     * @param bufferSize size of data array in bytes.
     */
    AbstractObjectReader(final int bufferSize) {
        this.buffer = new byte[bufferSize];
    }

    /**
     * Transform raw byte array to resulting type.
     *
     * @param array raw byte array
     * @return resulting object
     */
    protected abstract T getResultFromArray(@NotNull byte[] array);

    /**
     * Add data to processing object.
     *
     * @param byteBuffer buffer which contains data
     * @return count of read bytes
     */
    @Override
    public final int read(@NotNull final ByteBuffer byteBuffer) {
        if (buffer != null && curLength < buffer.length) {
            int getCount = Math.min(byteBuffer.remaining(),
                    buffer.length - curLength);
            byteBuffer.get(buffer, curLength, getCount);
            curLength += getCount;
            return getCount;
        }
        return 0;
    }

    /**
     * Reader is ready to get result.
     *
     * @return true if ready, false otherwise
     */
    @Override
    public final boolean isReady() {
        return buffer == null || buffer.length == curLength;
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
    public final T getResult() {
        if (!isReady()) {
            assert buffer != null;
            throw new InvalidProcessorStateException(
                    String.format("Current length %d less than required %d!",
                            curLength, buffer.length));
        }
        if (result == null) {
            assert buffer != null;
            result = getResultFromArray(buffer);
            buffer = null;
        }
        return result;
    }
}
