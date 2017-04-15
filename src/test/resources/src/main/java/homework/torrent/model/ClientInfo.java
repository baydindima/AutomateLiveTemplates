package homework.torrent.model;

import homework.torrent.model.reader.AbstractSingleReader;
import homework.torrent.model.reader.IntReader;
import homework.torrent.model.reader.ObjectReader;
import homework.torrent.model.reader.SequenceObjectReader;
import homework.torrent.model.writer.IntWriter;
import homework.torrent.model.writer.ObjectWriter;
import homework.torrent.model.writer.SeqObjectWriter;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * Info about client.
 */
@Data
public class ClientInfo implements SerializableObject, Serializable {
    /**
     * Ip-address of client. 4 byte.
     */
    @NotNull
    private final byte[] address;
    /**
     * Port of client.
     */
    private final int port;

    @NotNull
    @Override
    public ObjectWriter getWriter() {
        return new SeqObjectWriter(
                new IntWriter(ByteBuffer.wrap(address).getInt()),
                new IntWriter(port)
        );
    }

    public final static class Reader extends AbstractSingleReader<ClientInfo> {
        @NotNull
        private final IntReader ipReader = new IntReader();
        @NotNull
        private final IntReader portReader = new IntReader();
        @NotNull
        private final SequenceObjectReader seqReader = new SequenceObjectReader(ipReader, portReader);

        @NotNull
        @Override
        protected ClientInfo calcResult() {
            return new ClientInfo(
                    ByteBuffer.allocate(Integer.BYTES).putInt(ipReader.getResult()).array(),
                    portReader.getResult());
        }

        @NotNull
        @Override
        protected ObjectReader<?> getReader() {
            return seqReader;
        }

    }
}
