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
 * Sources response.
 */
@Data
public class SourcesServerResponse implements SerializableObject {
    /**
     * Clients which have file.
     */
    @NotNull
    private final List<ClientInfo> clients;

    /**
     * Writer of sources response.
     */
    @NotNull
    @Override
    public ObjectWriter getWriter() {
        return new ListObjectWriter<>(clients, ClientInfo::getWriter);
    }

    /**
     * Reader of sources response.
     */
    public final static class Reader extends AbstractSingleReader<SourcesServerResponse> {
        @NotNull
        private final ListObjectReader<ClientInfo> clientsReader =
                new ListObjectReader<>(ClientInfo.Reader::new);

        @NotNull
        @Override
        protected SourcesServerResponse calcResult() {
            return new SourcesServerResponse(clientsReader.getResult());
        }

        @NotNull
        @Override
        protected ObjectReader<?> getReader() {
            return clientsReader;
        }
    }
}
