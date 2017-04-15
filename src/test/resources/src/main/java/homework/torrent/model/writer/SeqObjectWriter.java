package homework.torrent.model.writer;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

/**
 * And for object writer
 */
public class SeqObjectWriter implements ObjectWriter {
    @NotNull
    private final ObjectWriter[] writers;
    private int curWriter = 0;

    public SeqObjectWriter(@NotNull ObjectWriter... writers) {
        this.writers = writers;
    }

    @Override
    public int write(@NotNull ByteBuffer byteBuffer) {
        int writeCount = 0;
        while (byteBuffer.hasRemaining() && curWriter < writers.length) {
            writeCount += writers[curWriter].write(byteBuffer);
            if (writers[curWriter].isComplete()) {
                curWriter += 1;
            }
        }
        return writeCount;
    }

    @Override
    public boolean isComplete() {
        return curWriter >= writers.length;
    }
}
