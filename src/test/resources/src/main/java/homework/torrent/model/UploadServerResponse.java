package homework.torrent.model;

import homework.torrent.model.reader.AbstractSingleReader;
import homework.torrent.model.reader.LongReader;
import homework.torrent.model.reader.ObjectReader;
import homework.torrent.model.writer.LongWriter;
import homework.torrent.model.writer.ObjectWriter;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

/**
 * Upload response.
 */
@Data
public class UploadServerResponse implements SerializableObject {
    /**
     * New id of file
     */
    private final long fileId;

    /**
     * Writer for upload response.
     */
    @NotNull
    @Override
    public ObjectWriter getWriter() {
        return new LongWriter(fileId);
    }

    /**
     * Reader for upload response
     */
    public static final class Reader extends AbstractSingleReader<UploadServerResponse> {
        @NotNull
        private final LongReader fileIdReader = new LongReader();

        @NotNull
        @Override
        protected UploadServerResponse calcResult() {
            return new UploadServerResponse(fileIdReader.getResult());
        }

        @NotNull
        @Override
        protected ObjectReader<?> getReader() {
            return fileIdReader;
        }
    }
}
