package homework.torrent.model.reader;

import homework.torrent.exception.InvalidProcessorStateException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Reader for collections
 */
public class ListObjectReader<T> implements ObjectReader<List<T>> {
    @NotNull
    private final IntReader sizeReader = new IntReader();
    @NotNull
    private final Supplier<ObjectReader<T>> readerSupplier;
    @Nullable
    private ObjectReader<T> curReader;
    @Nullable
    private List<T> result;

    public ListObjectReader(@NotNull final Supplier<ObjectReader<T>> readerSupplier) {
        this.readerSupplier = readerSupplier;
    }


    @Override
    public int read(@NotNull final ByteBuffer byteBuffer) {
        int readCount = 0;
        readCount += sizeReader.read(byteBuffer);
        if (sizeReader.isReady()) {
            if (result == null) {
                result = new ArrayList<>(sizeReader.getResult());
            }
            while (byteBuffer.hasRemaining()
                    && !isReady()) {
                if (curReader == null) {
                    curReader = readerSupplier.get();
                }
                readCount += curReader.read(byteBuffer);
                if (curReader.isReady()) {
                    result.add(curReader.getResult());
                    curReader = null;
                }
            }
        }
        return readCount;
    }

    @Override
    public boolean isReady() {
        return sizeReader.isReady()
                && result != null
                && sizeReader.getResult() == result.size()
                && (curReader == null || curReader.isReady());
    }

    @NotNull
    @Override
    public List<T> getResult() {
        if (!isReady()) {
            throw new InvalidProcessorStateException("List reader is not ready!");
        }
        return result;
    }
}
