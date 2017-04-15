package homework.torrent.model.reader;

import homework.torrent.exception.InvalidProcessorStateException;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * Or for object reader
 */
public class VariantObjectReader<CommonType> implements ObjectReader<CommonType> {
    @NotNull
    private final ByteBuffer variantBuffer;
    @NotNull
    private final List<ReaderVariant<? extends CommonType>> readerVariants;
    @Nullable
    private ObjectReader<? extends CommonType> chosenReader;

    public VariantObjectReader(int variantLength, ReaderVariant<? extends CommonType>... readers) {
        readerVariants = Arrays.asList(readers);
        variantBuffer = ByteBuffer.allocate(variantLength);
    }

    @Override
    public int read(@NotNull final ByteBuffer byteBuffer) {
        int readCount = 0;
        if (variantBuffer.hasRemaining()) {
            int putCount = Math.min(variantBuffer.remaining(), byteBuffer.remaining());
            int oldLimit = byteBuffer.limit();
            byteBuffer.limit(byteBuffer.position() + putCount);
            variantBuffer.put(byteBuffer);
            byteBuffer.limit(oldLimit);
            readCount += putCount;
        }
        if (!variantBuffer.hasRemaining()) {
            if (chosenReader == null) {
                chosenReader = getChosenReader();
                variantBuffer.flip();
                chosenReader.read(variantBuffer);
            }
            readCount += chosenReader.read(byteBuffer);
        }
        return readCount;
    }

    @NotNull
    private ObjectReader<? extends CommonType> getChosenReader() {
        for (ReaderVariant<? extends CommonType> readerVariant : readerVariants) {
            if (readerVariant.isThatType.test(variantBuffer)) {
                return readerVariant.reader;
            }
        }
        throw new InvalidProcessorStateException("No one reader did not approach!");
    }

    @Override
    public boolean isReady() {
        return chosenReader != null && chosenReader.isReady();
    }

    @NotNull
    @Override
    public CommonType getResult() {
        if (!isReady() || chosenReader == null) {
            throw new InvalidProcessorStateException("Processor is not ready!");
        }
        return chosenReader.getResult();
    }

    @Data
    public static class ReaderVariant<T> {
        @NotNull
        private final ObjectReader<T> reader;
        @NotNull
        private final Predicate<ByteBuffer> isThatType;
    }

}
