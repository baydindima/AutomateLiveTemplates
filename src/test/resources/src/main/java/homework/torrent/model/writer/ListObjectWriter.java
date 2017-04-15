package homework.torrent.model.writer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Function;

/**
 * Object writer for collections
 */
public class ListObjectWriter<T> implements ObjectWriter {
    @NotNull
    private final IntWriter sizeWriter;
    @NotNull
    private final Iterator<T> iterator;
    @NotNull
    private final Function<T, ObjectWriter> toWriter;
    @Nullable
    private ObjectWriter curWriter;

    public ListObjectWriter(@NotNull final Collection<T> collection,
                            @NotNull final Function<T, ObjectWriter> toWriter) {
        sizeWriter = new IntWriter(collection.size());
        iterator = collection.iterator();
        this.toWriter = toWriter;
    }

    @Override
    public int write(@NotNull ByteBuffer byteBuffer) {
        int writeCount = 0;
        writeCount += sizeWriter.write(byteBuffer);
        while (byteBuffer.hasRemaining()
                && !isComplete()) {
            if (curWriter == null) {
                curWriter = toWriter.apply(iterator.next());
            }
            writeCount += curWriter.write(byteBuffer);
            if (curWriter.isComplete()) {
                curWriter = null;
            }
        }
        return writeCount;
    }

    @Override
    public boolean isComplete() {
        return sizeWriter.isComplete()
                && !iterator.hasNext()
                && (curWriter == null || curWriter.isComplete());
    }
}
