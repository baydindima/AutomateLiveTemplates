package homework.torrent.model;

import homework.torrent.model.reader.AbstractSingleReader;
import homework.torrent.model.reader.BooleanReader;
import homework.torrent.model.reader.ObjectReader;
import homework.torrent.model.writer.BooleanWriter;
import homework.torrent.model.writer.ObjectWriter;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

/**
 * Update response.
 */
@Data
public class UpdateServerResponse implements SerializableObject {
    /**
     * Status of response.
     */
    private final boolean status;

    /**
     * Writer for update response.
     */
    @NotNull
    @Override
    public ObjectWriter getWriter() {
        return new BooleanWriter(status);
    }


    /**
     * Reader for update response.
     */
    public final static class Reader extends AbstractSingleReader<UpdateServerResponse> {
        @NotNull
        private final BooleanReader statusReader = new BooleanReader();

        @NotNull
        @Override
        protected UpdateServerResponse calcResult() {
            return new UpdateServerResponse(statusReader.getResult());
        }

        @NotNull
        @Override
        protected ObjectReader<?> getReader() {
            return statusReader;
        }
    }


}
