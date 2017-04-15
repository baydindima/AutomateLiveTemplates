package homework.torrent.model.writer;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

/**
 * Helping with asynchronous writing data.
 * Can add data to byte array with small pieces.
 * Can not been reset
 */
public interface ObjectWriter {

    /**
     * Write to byteBuffer min of remaining object size or buffer space.
     *
     * @param byteBuffer buffer for writing
     * @return count of written bytes
     */
    int write(@NotNull ByteBuffer byteBuffer);

    /**
     * Is writer completed writing of object.
     *
     * @return true if writing completed, false otherwise
     */
    boolean isComplete();

}
