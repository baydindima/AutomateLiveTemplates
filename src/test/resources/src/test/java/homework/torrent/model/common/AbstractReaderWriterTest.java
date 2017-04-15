package homework.torrent.model.common;

import homework.torrent.model.reader.ObjectReader;
import homework.torrent.model.writer.ObjectWriter;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;

/**
 * Abstract tests of asynchronous read/write
 */
public class AbstractReaderWriterTest<T> {
    private static final int SMALL_BUFFER_SIZE = 2;
    private static final int MEDIUM_BUFFER_SIZE = 16;
    private static final int BIG_BUFFER_SIZE = 1024;

    private final @NotNull Supplier<ObjectReader<? super T>> readerSupplier;
    private final @NotNull Function<T, ObjectWriter> writerFunction;

    public AbstractReaderWriterTest(@NotNull Supplier<ObjectReader<? super T>> readerSupplier,
                                    @NotNull Function<T, ObjectWriter> writerFunction) {
        this.readerSupplier = readerSupplier;
        this.writerFunction = writerFunction;
    }

    public void test(T testDate) {
        differentBufSizeTest(testDate);
        manyGetResultTest(testDate);
    }

    private void differentBufSizeTest(T testData) {
        assertEquals(testData, readWrite(testData, SMALL_BUFFER_SIZE).getResult());
        assertEquals(testData, readWrite(testData, MEDIUM_BUFFER_SIZE).getResult());
        assertEquals(testData, readWrite(testData, BIG_BUFFER_SIZE).getResult());
    }

    private void manyGetResultTest(T testData) {
        ObjectReader<? super T> reader = readWrite(testData, MEDIUM_BUFFER_SIZE);
        assertEquals(testData, reader.getResult());
        assertEquals(testData, reader.getResult());
        assertEquals(testData, reader.getResult());
    }


    public ObjectReader<? super T> readWrite(T testData, int byteBufferSize) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(byteBufferSize);
        ObjectWriter writer = writerFunction.apply(testData);
        ObjectReader<? super T> reader = readerSupplier.get();

        while (!reader.isReady()) {
            byteBuffer.clear();
            writer.write(byteBuffer);
            byteBuffer.flip();
            reader.read(byteBuffer);
        }

        return reader;
    }
}
