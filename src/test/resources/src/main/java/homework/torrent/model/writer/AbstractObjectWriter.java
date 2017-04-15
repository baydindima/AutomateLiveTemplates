package homework.torrent.model.writer;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

/**
 * Abstract writer, for object with small size.
 */
abstract class AbstractObjectWriter implements ObjectWriter {
    /**
     * Data for writing.
     */
    @NotNull
    private final byte[] data;
    /**
     * Current length of written part.
     */
    private int curLength = 0;

    /**
     * Create new instance of abstract object writer.
     *
     * @param value inner array for writing
     */
    AbstractObjectWriter(@NotNull final byte[] value) {
        this.data = value;
    }

    /**
     * Get length of inner array.
     *
     * @return length of inner array
     */
    int getDataLength() {
        return data.length;
    }


    /**
     * Write to byteBuffer min of remaining object size or buffer space.
     *
     * @param byteBuffer buffer for writing
     * @return count of written bytes
     */
    @Override
    public int write(@NotNull final ByteBuffer byteBuffer) {
        if (curLength < data.length) {
            int putCount = Math.min(data.length - curLength,
                    byteBuffer.remaining());
            byteBuffer.put(data, curLength, putCount);
            curLength += putCount;
            return putCount;
        }
        return 0;
    }

    /**
     * Is writer completed writing of object.
     *
     * @return true if writing completed, false otherwise
     */
    @Override
    public boolean isComplete() {
        return curLength == data.length;
    }
}
