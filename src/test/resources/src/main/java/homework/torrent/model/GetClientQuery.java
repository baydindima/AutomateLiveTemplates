package homework.torrent.model;

import homework.torrent.exception.InvalidQueryFormat;
import homework.torrent.model.reader.*;
import homework.torrent.model.writer.*;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

/**
 * Get query.
 */
@Data
public class GetClientQuery implements SerializableObject, TorrentClientQuery {
    /**
     * Id of file.
     */
    private final long fileId;
    /**
     * Number of part.
     */
    private final int partId;

    @Override
    public @NotNull TorrentClientQuery.Type getType() {
        return Type.Get;
    }

    /** Writer of get query. */
    @NotNull
    @Override
    public ObjectWriter getWriter() {
        return new SeqObjectWriter(new ByteWriter(getType().getId()),
                new LongWriter(fileId),
                new IntWriter(partId));
    }

    /**
     * Reader of get query.
     */
    public final static class Reader extends AbstractSingleReader<GetClientQuery> {
        @NotNull
        private final ByteReader typeReader = new ByteReader();
        @NotNull
        private final LongReader fileIdReader = new LongReader();
        @NotNull
        private final IntReader partIdReader = new IntReader();
        @NotNull
        private final SequenceObjectReader seqReader = new SequenceObjectReader(typeReader, fileIdReader, partIdReader);

        @NotNull
        @Override
        protected GetClientQuery calcResult() {
            if (typeReader.getResult() != Type.Get.getId()) {
                throw new InvalidQueryFormat(
                        String.format("Expected get type, but got %s",
                                typeReader.getResult()));
            }
            return new GetClientQuery(fileIdReader.getResult(), partIdReader.getResult());
        }

        @NotNull
        @Override
        protected ObjectReader<?> getReader() {
            return seqReader;
        }
    }
}
