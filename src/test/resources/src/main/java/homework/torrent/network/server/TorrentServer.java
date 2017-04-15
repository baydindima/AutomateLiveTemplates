package homework.torrent.network.server;

import homework.torrent.app.TorrentTracker;
import homework.torrent.model.*;
import homework.torrent.model.reader.ObjectReader;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Torrent server.
 */
@Slf4j
public class TorrentServer extends Server<TorrentServerQuery> {
    public final static int TORRENT_PORT = 8081;

    @NotNull
    private final TorrentTracker torrentTracker;

    /**
     * Create new instance of server, that listening specified port.
     */
    public TorrentServer(@NotNull final TorrentTracker torrentTracker) {
        super(TORRENT_PORT);
        this.torrentTracker = torrentTracker;
    }

    @NotNull
    @Override
    protected Supplier<ObjectReader<TorrentServerQuery>> getReaderSupplier() {
        return TorrentServerQuery::getReader;
    }

    @NotNull
    @Override
    protected Consumer<AsynchronousSocketChannel> processMessage(
            @NotNull final TorrentServerQuery query,
            @NotNull final InetAddress address) {
        try {
            switch (query.getType()) {
                case List:
                    return defaultWrite(processListQuery().getWriter());
                case Upload:
                    return defaultWrite(processUploadQuery((UploadServerQuery) query).getWriter());
                case Source:
                    return defaultWrite(processSourcesQuery((SourcesServerQuery) query).getWriter());
                case Update:
                    return defaultWrite(processUpdateQuery((UpdateServerQuery) query, address).getWriter());
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
    private ListServerResponse processListQuery() {
        log.info("Start processing list query.");
        return new ListServerResponse(torrentTracker.getFileList());
    }

    @NotNull
    private UploadServerResponse processUploadQuery(@NotNull final UploadServerQuery query) {
        log.info("Start processing upload query.");
        return new UploadServerResponse(torrentTracker.uploadFile(query.getFileName(), query.getFileSize()));
    }

    @NotNull
    private SourcesServerResponse processSourcesQuery(@NotNull final SourcesServerQuery query) {
        log.info("Start processing sources query.");
        return new SourcesServerResponse(torrentTracker.getSources(query.getFileId()));
    }

    @NotNull
    private UpdateServerResponse processUpdateQuery(@NotNull final UpdateServerQuery query,
                                                    @NotNull final InetAddress inetAddress) {
        log.info("Start processing update query.");
        byte[] address = inetAddress.getAddress();
        if (address.length != 4) {
            log.error("Invalid format for inet address: {}", inetAddress);
            return new UpdateServerResponse(false);
        }

        try {
            torrentTracker.updateClientInfo(new ClientInfo(address, query.getPort()), query.getFileIds());
            return new UpdateServerResponse(true);
        } catch (Exception e) {
            log.error("Error during updating client {} data", inetAddress, e);
            return new UpdateServerResponse(false);
        }
    }

}
