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
 * Sources query.
 */
@Data
public class SourcesServerQuery implements SerializableObject, TorrentServerQuery {
    /**
     * Id of file
     */
    private final long fileId;

    @Override
    public @NotNull TorrentServerQuery.Type getType() {
        return Type.Source;
    }

    /**
     * Writer of sources query.
     */
    @NotNull
    @Override
    public ObjectWriter getWriter() {
        return new SeqObjectWriter(
                new ByteWriter(Type.Source.getId()),
                new LongWriter(fileId)
        );
    }

    /**
     * Reader of sources query.
     */
    public final static class Reader extends AbstractSingleReader<SourcesServerQuery> {
        @NotNull
        private final ByteReader typeReader = new ByteReader();
        @NotNull
        private final LongReader fileIdReader = new LongReader();
        @NotNull
        private final SequenceObjectReader seqReader = new SequenceObjectReader(typeReader, fileIdReader);

        @NotNull
        @Override
        protected SourcesServerQuery calcResult() {
            if (typeReader.getResult() != Type.Source.getId()) {
                throw new InvalidQueryFormat(
                        String.format("Expected sources type, but got %s",
                                typeReader.getResult()));
            }
            return new SourcesServerQuery(fileIdReader.getResult());
        }

        @NotNull
        @Override
        protected ObjectReader<?> getReader() {
            return seqReader;
        }
    }
}
