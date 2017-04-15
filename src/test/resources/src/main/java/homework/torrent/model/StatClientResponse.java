package homework.torrent.model;

import homework.torrent.model.reader.AbstractSingleReader;
import homework.torrent.model.reader.IntReader;
import homework.torrent.model.reader.ListObjectReader;
import homework.torrent.model.reader.ObjectReader;
import homework.torrent.model.writer.IntWriter;
import homework.torrent.model.writer.ListObjectWriter;
import homework.torrent.model.writer.ObjectWriter;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Stat response.
 */
@Data
public class StatClientResponse implements SerializableObject {
    /**
     * Nums of available parts.
     */
    @NotNull
    private final List<Integer> partNums;

    /**
     * Writer of stat response.
     */
    @NotNull
    @Override
    public ObjectWriter getWriter() {
        return new ListObjectWriter<>(partNums, IntWriter::new);
    }


    /**
     * Reader of stat response.
     */
    public final static class Reader extends AbstractSingleReader<StatClientResponse> {
        @NotNull
        private final ListObjectReader<Integer> partsReader = new ListObjectReader<>(IntReader::new);

        @NotNull
        @Override
        protected StatClientResponse calcResult() {
            return new StatClientResponse(partsReader.getResult());
        }

        @NotNull
        @Override
        protected ObjectReader<?> getReader() {
            return partsReader;
        }
    }
}
