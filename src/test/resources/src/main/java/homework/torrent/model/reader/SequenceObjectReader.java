package homework.torrent.model.reader;

import org.jetbrains.annotations.NotNull;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.nio.ByteBuffer;

/**
 * And for object reader
 */
public class SequenceObjectReader implements ObjectReader {
    @NotNull
    private final ObjectReader[] readers;
    private int curReader = 0;

    public SequenceObjectReader(@NotNull final ObjectReader<?>... readers) {
        this.readers = readers;
    }

    @Override
    public int read(@NotNull final ByteBuffer byteBuffer) {
        int readCount = 0;
        while (byteBuffer.hasRemaining() && curReader < readers.length) {
            readCount += readers[curReader].read(byteBuffer);
            if (readers[curReader].isReady()) {
                curReader += 1;
            }
        }
        return readCount;
    }

    @Override
    public boolean isReady() {
        return curReader == readers.length;
    }

    @NotNull
    @Override
    public Object getResult() {
        throw new NotImplementedException();
    }
}
