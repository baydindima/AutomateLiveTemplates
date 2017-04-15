package homework.torrent.model;

import homework.torrent.exception.InvalidQueryFormat;
import homework.torrent.model.reader.*;
import homework.torrent.model.writer.*;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Update query.
 */
@Data
public class UpdateServerQuery implements SerializableObject, TorrentServerQuery {
    /**
     * Port of client.
     */
    private final int port;
    /**
     * Id-s of available parts.
     */
    @NotNull
    private final List<Long> fileIds;

    @Override
    public @NotNull TorrentServerQuery.Type getType() {
        return Type.Update;
    }

    /**
     * Writer for update query
     */
    @NotNull
    @Override
    public ObjectWriter getWriter() {
        return new SeqObjectWriter(
                new ByteWriter(Type.Update.getId()),
                new IntWriter(port),
                new ListObjectWriter<>(fileIds, LongWriter::new)
        );
    }

    /**
     * Reader for update query
     */
    public final static class Reader extends AbstractSingleReader<UpdateServerQuery> {
        @NotNull
        private final ByteReader typeReader = new ByteReader();
        @NotNull
        private final IntReader portReader = new IntReader();
        @NotNull
        private final ListObjectReader<Long> fileIdsReader = new ListObjectReader<>(LongReader::new);
        @NotNull
        private final SequenceObjectReader seqReader = new SequenceObjectReader(typeReader, portReader, fileIdsReader);

        @NotNull
        @Override
        protected UpdateServerQuery calcResult() throws InvalidQueryFormat {
            if (typeReader.getResult() != Type.Update.getId()) {
                throw new InvalidQueryFormat(String.format("Expected update type, but got %s", typeReader.getResult()));
            }
            return new UpdateServerQuery(portReader.getResult(), fileIdsReader.getResult());
        }

        @NotNull
        @Override
        protected ObjectReader<?> getReader() {
            return seqReader;
        }
    }
}
