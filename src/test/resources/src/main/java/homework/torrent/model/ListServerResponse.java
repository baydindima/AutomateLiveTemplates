package homework.torrent.model;

import homework.torrent.model.reader.AbstractSingleReader;
import homework.torrent.model.reader.ListObjectReader;
import homework.torrent.model.reader.ObjectReader;
import homework.torrent.model.writer.ListObjectWriter;
import homework.torrent.model.writer.ObjectWriter;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * List response.
 */
@Data
public class ListServerResponse implements SerializableObject {
    /**
     * Available files.
     */
    @NotNull
    private final List<FileInfo> files;


    @NotNull
    @Override
    public ObjectWriter getWriter() {
        return new ListObjectWriter<>(files, FileInfo::getWriter);
    }

    public final static class Reader extends AbstractSingleReader<ListServerResponse> {
        @NotNull
        private final ListObjectReader<FileInfo> filesReader = new ListObjectReader<>(FileInfo.Reader::new);

        @NotNull
        @Override
        protected ListServerResponse calcResult() {
            return new ListServerResponse(filesReader.getResult());
        }

        @NotNull
        @Override
        protected ObjectReader<?> getReader() {
            return filesReader;
        }
    }
}
