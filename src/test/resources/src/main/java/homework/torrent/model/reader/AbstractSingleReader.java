package homework.torrent.model.reader;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;

/**
 * Abstract reader for single reader.
 */
public abstract class AbstractSingleReader<T> implements ObjectReader<T> {
    @Nullable
    private T result;

    /**
     * @return result of reading.
     */
    @NotNull
    protected abstract T calcResult();

    /**
     * @return single reader
     */
    @NotNull
    protected abstract ObjectReader<?> getReader();

    /**
     * Add data to processing object.
     *
     * @param byteBuffer buffer which contains data
     * @return count of read bytes
     */
    @Override
    public final int read(@NotNull final ByteBuffer byteBuffer) {
        return getReader().read(byteBuffer);
    }

    /**
     * Reader is ready to get result.
     *
     * @return true if ready, false otherwise
     */
    @Override
    public final boolean isReady() {
        return getReader().isReady();
    }

    /**
     * Return reader result.
     */
    @NotNull
    @Override
    public final T getResult() {
        if (result == null) {
            result = calcResult();
        }
        return result;
    }
}
