package homework.torrent.model.writer;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

/**
 * Object writer for string with size in first.
 */
public class StringWriter implements ObjectWriter {
    /**
     * Writer for raw string.
     */
    @NotNull
    private final StringWithoutSizeWriter stringWithoutSizeWriter;
    /**
     * Writer for size of string.
     */
    @NotNull
    private final IntWriter sizeWriter;

    /**
     * Create new instance of StringWriter.
     *
     * @param string string for writing
     */
    public StringWriter(@NotNull final String string) {
        stringWithoutSizeWriter = new StringWithoutSizeWriter(string);
        sizeWriter = new IntWriter(stringWithoutSizeWriter.getDataLength());
    }

    /**
     * Write to byteBuffer min of remaining object size or buffer space.
     *
     * @param byteBuffer buffer for writing
     * @return count of written bytes
     */
    @Override
    public final int write(@NotNull final ByteBuffer byteBuffer) {
        return sizeWriter.write(byteBuffer)
                + stringWithoutSizeWriter.write(byteBuffer);
    }

    /**
     * Is writer completed writing of object.
     *
     * @return true if writing completed, false otherwise
     */
    @Override
    public final boolean isComplete() {
        return sizeWriter.isComplete() && stringWithoutSizeWriter.isComplete();
    }
}
