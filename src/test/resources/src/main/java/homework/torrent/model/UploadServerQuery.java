package homework.torrent.model;

import homework.torrent.exception.InvalidQueryFormat;
import homework.torrent.model.reader.*;
import homework.torrent.model.writer.*;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

/**
 * Upload query.
 */
@Data
public class UploadServerQuery implements SerializableObject, TorrentServerQuery {
    /**
     * Name of file
     */
    @NotNull
    private final String fileName;
    /**
     * Size of file in bytes
     */
    private final long fileSize;

    @Override
    public @NotNull TorrentServerQuery.Type getType() {
        return Type.Upload;
    }

    /** Writer for upload query */
    @NotNull
    @Override
    public ObjectWriter getWriter() {
        return new SeqObjectWriter(
                new ByteWriter(Type.Upload.getId()),
                new StringWriter(fileName),
                new LongWriter(fileSize));
    }

    /**
     * Reader for upload query
     */
    public static class Reader extends AbstractSingleReader<UploadServerQuery> {
        @NotNull
        private final ByteReader typeReader = new ByteReader();
        @NotNull
        private final StringReader fileNameReader = new StringReader();
        @NotNull
        private final LongReader sizeReader = new LongReader();
        @NotNull
        private final SequenceObjectReader seqReader = new SequenceObjectReader(typeReader, fileNameReader, sizeReader);

        @NotNull
        @Override
        protected UploadServerQuery calcResult() {
            if (typeReader.getResult() != Type.Upload.getId()) {
                throw new InvalidQueryFormat(String.format("Expected upload type, but got %s", typeReader.getResult()));
            }
            return new UploadServerQuery(
                    fileNameReader.getResult(),
                    sizeReader.getResult()
            );
        }

        @NotNull
        @Override
        protected ObjectReader<?> getReader() {
            return seqReader;
        }
    }
}
