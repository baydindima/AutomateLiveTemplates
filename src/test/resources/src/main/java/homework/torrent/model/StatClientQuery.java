package homework.torrent.model;

import homework.torrent.exception.InvalidQueryFormat;
import homework.torrent.model.reader.*;
import homework.torrent.model.writer.ByteWriter;
import homework.torrent.model.writer.LongWriter;
import homework.torrent.model.writer.ObjectWriter;
import homework.torrent.model.writer.SeqObjectWriter;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

/**
 * Stat query.
 */
@Data
public class StatClientQuery implements TorrentClientQuery, SerializableObject {
    /**
     * File id
     */
    private final long fileId;

    @Override
    public @NotNull Type getType() {
        return Type.Stat;
    }

    /**
     * Writer of stat query.
     */
    @NotNull
    @Override
    public ObjectWriter getWriter() {
        return new SeqObjectWriter(
                new ByteWriter(getType().getId()),
                new LongWriter(fileId)
        );
    }

    /**
     * Reader of stat query.
     */
    public final static class Reader extends AbstractSingleReader<StatClientQuery> {
        @NotNull
        private final ByteReader typeReader = new ByteReader();
        @NotNull
        private final LongReader idReader = new LongReader();
        @NotNull
        private final SequenceObjectReader seqReader = new SequenceObjectReader(typeReader, idReader);

        @NotNull
        @Override
        protected StatClientQuery calcResult() {
            if (typeReader.getResult() != Type.Stat.getId()) {
                throw new InvalidQueryFormat(String.format("Expected stat type, but got %s", typeReader.getResult()));
            }
            return new StatClientQuery(idReader.getResult());
        }

        @NotNull
        @Override
        protected ObjectReader<?> getReader() {
            return seqReader;
        }
    }
}
