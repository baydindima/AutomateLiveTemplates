package homework.torrent.app;

import homework.torrent.model.UpdateServerQuery;
import homework.torrent.model.UpdateServerResponse;
import homework.torrent.network.AsynchronousRequest;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.nio.channels.CompletionHandler;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * This class send message to server with info about the client.
 */
@Slf4j
public class ClientScheduledUpdater {

    /**
     * Delay between update queries in secs.
     */
    private final static long UPDATE_DELAY_SEC = 20;
    /**
     * Completion handler after response from server.
     */
    @NotNull
    private static final
    CompletionHandler<UpdateServerResponse, Void> afterResponse =
            new CompletionHandler<UpdateServerResponse, Void>() {
                @Override
                public void completed(@NotNull
                                      final UpdateServerResponse result,
                                      @Nullable
                                      final Void attachment) {
                    log.info("Update completed with result status: {}",
                            result.isStatus());
                }

                @Override
                public void failed(@NotNull
                                   final Throwable exc,
                                   @Nullable
                                   final Void attachment) {
                    log.error(
                            "Error while receiving message from server!",
                            exc);
                }
            };
    /**
     * Client storage.
     */
    @NotNull
    private final TorrentClientStorageExternal torrentClientStorage;
    /**
     * Inet address of server
     */
    @NotNull
    private final InetSocketAddress serverAddress;
    /**
     * Listening port of client's
     */
    private final int clientPort;
    @NotNull
    private ScheduledFuture<?> scheduledFuture;


    /**
     * This class send message to server with info about the client.
     */
    public ClientScheduledUpdater(@NotNull TorrentClientStorageExternal torrentClientStorage, @NotNull InetSocketAddress serverAddress, int clientPort) {
        this.torrentClientStorage = torrentClientStorage;
        this.serverAddress = serverAddress;
        this.clientPort = clientPort;
    }

    /**
     * Starts sending update queries to server.
     */
    public final void start() {
        scheduledFuture = Executors.newScheduledThreadPool(1)
                .scheduleWithFixedDelay(
                        this::updateData,
                        0,
                        UPDATE_DELAY_SEC,
                        TimeUnit.SECONDS);

    }

    /**
     * Stop sending update queries to server.
     */
    public final void stop() {
        scheduledFuture.cancel(true);
    }

    /**
     * Sends update query
     */
    private void updateData() {
        List<Long> filesList = torrentClientStorage.getFilesIdList();
        AsynchronousRequest.builder()
                .inetAddress(serverAddress)
                .query(new UpdateServerQuery(clientPort, filesList))
                .onFailure(throwable ->
                        log.error("Error while sending update query to server!",
                                throwable))
                .responseReader(
                        AsynchronousRequest
                                .defaultRead(
                                        new UpdateServerResponse.Reader(),
                                        afterResponse)
                )
                .build()
                .execute();
    }

}
