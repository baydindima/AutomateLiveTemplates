package homework.torrent.network.server;

import homework.torrent.app.TorrentClientStorageExternal;
import homework.torrent.exception.NoSuchFileException;
import homework.torrent.exception.NoSuchPartException;
import homework.torrent.model.*;
import homework.torrent.model.reader.ObjectReader;
import homework.torrent.model.writer.AsynchronousFileWriter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Client server.
 */
@Slf4j
public class ClientServer extends Server<TorrentClientQuery> {

    @NotNull
    private final TorrentClientStorageExternal torrentClientStorageExternal;

    /**
     * Create new instance of server, that listening specified port.
     *
     * @param listeningPort                port for listening
     * @param torrentClientStorageExternal torrent client
     */
    public ClientServer(int listeningPort, @NotNull TorrentClientStorageExternal torrentClientStorageExternal) {
        super(listeningPort);
        this.torrentClientStorageExternal = torrentClientStorageExternal;
    }

    @NotNull
    @Override
    protected Supplier<ObjectReader<TorrentClientQuery>> getReaderSupplier() {
        return TorrentClientQuery::getReader;
    }

    @NotNull
    @Override
    protected Consumer<AsynchronousSocketChannel> processMessage(@NotNull final TorrentClientQuery query,
                                                                 @NotNull final InetAddress inetAddress) {
        try {
            switch (query.getType()) {
                case Get:
                    return processGetQuery((GetClientQuery) query);
                case Stat:
                    return defaultWrite(processStatQuery((StatClientQuery) query).getWriter());
                default:
                    log.error("Missing processing statement for query: {}", query);
                    return this::closeConnection;
            }
        } catch (Exception e) {
            log.error("Exception during processing query", e);
            return this::closeConnection;
        }
    }

    @NotNull
    private StatClientResponse processStatQuery(@NotNull final StatClientQuery query) {
        return new StatClientResponse(torrentClientStorageExternal.getStat(query.getFileId()));
    }

    @NotNull
    private Consumer<AsynchronousSocketChannel> processGetQuery(@NotNull final GetClientQuery query) throws NoSuchFileException, NoSuchPartException {
        @NotNull FilePart part = torrentClientStorageExternal.getPart(query.getFileId(), query.getPartId());
        return asynchronousSocketChannel ->
                AsynchronousFileWriter.builder()
                        .outputChannel(asynchronousSocketChannel)
                        .filePath(part.getFilePath())
                        .length(part.getLength())
                        .offset(part.getOffset())
                        .build()
                        .write();
    }


}
