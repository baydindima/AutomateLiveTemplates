package homework.torrent.model.reader;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

/**
 * Helping with asynchronous receiving data.
 * Can add data to object with small pieces.
 * When object is ready, may return completed object with getResult method.
 * Can not been reset
 *
 * @param <T> result type
 */
public interface ObjectReader<T> {

    /**
     * Add data to processing object.
     *
     * @param byteBuffer buffer which contains data
     * @return count of read bytes
     */
    int read(@NotNull ByteBuffer byteBuffer);

    /**
     * Reader is ready to get result.
     *
     * @return true if ready, false otherwise
     */
    boolean isReady();

    /**
     * Return reader result.
     *
     * @return completed object
     * @throws InvalidProcessorStateException if processor not ready
     *                                        for creation of object
     */
    @NotNull T getResult();

}

