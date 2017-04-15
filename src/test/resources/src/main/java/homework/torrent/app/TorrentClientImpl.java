package homework.torrent.app;

import homework.torrent.exception.FileAlreadyContainsInStorage;
import homework.torrent.exception.NoSuchPartException;
import homework.torrent.model.*;
import homework.torrent.model.reader.AsynchronousFileReader;
import homework.torrent.network.AsynchronousRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.CompletionHandler;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * API for user's commands
 */
@Slf4j
@AllArgsConstructor
public class TorrentClientImpl implements TorrentClient {

    @NotNull
    private final InetSocketAddress serverAddress;

    @NotNull
    private final TorrentClientStorageInternal storage;

    @NotNull
    private <T> CompletionHandler<T, Void>
    futureHandler(CompletableFuture<T> future) {
        return new CompletionHandler
                <T, Void>() {
            @Override
            public void completed(@Nullable
                                  final T result,
                                  @Nullable Void v) {
                future.complete(result);
            }

            @Override
            public void failed(@NotNull
                               final Throwable exc,
                               @Nullable Void v) {
                log.error("Failed", exc);
                future.completeExceptionally(exc);
            }
        };
    }

    /**
     * Returns meta-info about all available files in torrent's network.
     *
     * @return meta-info about all available files
     */
    @NotNull
    @Override
    public Future<List<FileInfo>> getAvailableFiles() {
        CompletableFuture<ListServerResponse> completableFuture =
                new CompletableFuture<>();
        AsynchronousRequest.builder()
                .inetAddress(serverAddress)
                .query(new ListServerQuery())
                .responseReader(
                        AsynchronousRequest.defaultRead(
                                new ListServerResponse.Reader(),
                                futureHandler(completableFuture)
                        ))
                .onFailure(completableFuture::completeExceptionally)
                .build()
                .execute();
        return completableFuture.thenApply(ListServerResponse::getFiles);
    }

    /**
     * Add file to torrent's network.
     * Send upload query to server and add info about this file to storage.
     *
     * @param filePath path of fileiterator.next();
     * @return future of completion
     * @throws NoSuchFileException if file not exists
     */
    @NotNull
    @Override
    public Future<FileInfo> addFileToTorrent(@NotNull Path filePath)
            throws NoSuchFileException {
        File file = filePath.toFile();
        if (!file.exists()
                && file.isDirectory()) {
            throw new NoSuchFileException(filePath.toString());
        }
        long length = file.length();
        String fileName = filePath.getFileName().toString();
        CompletableFuture<UploadServerResponse> future =
                new CompletableFuture<>();
        AsynchronousRequest.builder()
                .inetAddress(serverAddress)
                .query(new UploadServerQuery(fileName, length))
                .responseReader(
                        AsynchronousRequest.defaultRead(
                                new UploadServerResponse.Reader(),
                                futureHandler(future)
                        ))
                .onFailure(future::completeExceptionally)
                .build()
                .execute();

        return future.thenApply(response -> {
                    FileInfo fileInfo = new FileInfo(response.getFileId(), fileName, length);
                    storage.addExistingFile(filePath, fileInfo);
                    return fileInfo;
                }

        );
    }

    @Override
    public ProgressListener downloadFile(@NotNull Path destinationPath,
                                         @NotNull FileInfo fileInfo)
            throws IOException, FileAlreadyContainsInStorage {
        if (storage.contains(fileInfo.getId())) {
            throw new FileAlreadyContainsInStorage();
        }

        File file = destinationPath.toFile();
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw")) {
            randomAccessFile.setLength(fileInfo.getSize());
        }

        ProgressListener progressListener = new ProgressListener(fileInfo.getSize());

        storage.addNotLoadedFile(destinationPath, fileInfo);

        CompletableFuture<SourcesServerResponse> future =
                getSourcesFuture(fileInfo.getId());

        future.thenCompose(sourcesServerResponse -> {
            List<CompletableFuture<StatResponseWithClientInfo>> futures =
                    sourcesServerResponse.getClients().parallelStream()
                            .map(clientInfo -> getStatRequest(clientInfo, fileInfo.getId()))
                            .collect(Collectors.toList());

            return getPartToClientMap(futures).thenCompose(map -> {
                List<CompletableFuture<Void>> collect = map.entrySet().stream().map(entry ->
                        loadPart(entry.getValue(), progressListener, fileInfo.getId(), entry.getKey())
                ).collect(Collectors.toList());

                return CompletableFuture.allOf(collect.toArray(new CompletableFuture[collect.size()]));
            });
        });

        return progressListener;
    }

    private CompletableFuture<Void> loadPart(
            @NotNull
            final List<ClientInfo> clients,
            @NotNull
            final ProgressListener progressListener,
            final long fileId,
            final int partId) {


        CompletableFuture<Void> future = new CompletableFuture<>();
        Iterator<ClientInfo> iterator = clients.iterator();
        ClientInfo clientInfo = iterator.next();

        try {
            FilePart part = storage.getPart(fileId, partId);
            InetSocketAddress inetSocketAddress =
                    getAddressFromClientInfo(clientInfo);

            AsynchronousRequest.builder()
                    .inetAddress(inetSocketAddress)
                    .query(new GetClientQuery(fileId, partId))
                    .responseReader(
                            requestAttachment ->
                                    AsynchronousFileReader.builder()
                                            .length(part.getLength())
                                            .offset(part.getOffset())
                                            .filePath(part.getFilePath())
                                            .progressListener(progressListener)
                                            .socketChannel(requestAttachment
                                                    .getSocketChannel())
                                            .completionHandler(
                                                    futureHandler(future))
                                            .build()
                                            .read()
                    )
                    .onFailure(future::completeExceptionally)
                    .build()
                    .execute();
            return future;
        } catch (NoSuchPartException | homework.torrent.exception.NoSuchFileException | UnknownHostException e) {
            log.error("Load part exception!", e);
            future.completeExceptionally(e);
            return future;
        }
    }

    @NotNull
    private CompletableFuture<Map<Integer, List<ClientInfo>>>
    getPartToClientMap(@NotNull
                       final List<CompletableFuture<StatResponseWithClientInfo>>
                               futures) {
        return CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[futures.size()])
        ).thenApply(_1 -> {
            ConcurrentMap<Integer, List<ClientInfo>> clients = new ConcurrentHashMap<>();
            futures.forEach(statResponse -> {
                try {
                    StatResponseWithClientInfo info = statResponse.get();
                    info.getStatClientResponse().getPartNums().forEach(partNum ->
                            clients.compute(partNum, (part, clientList) -> {
                                if (clientList == null) {
                                    List<ClientInfo> list = new LinkedList<>();
                                    list.add(info.getClientInfo());
                                    return list;
                                } else {
                                    clientList.add(info.getClientInfo());
                                    return clientList;
                                }
                            })
                    );
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Error during get stat request to {}", e);
                }
            });
            return clients;
        });
    }

    @NotNull
    private CompletableFuture<SourcesServerResponse> getSourcesFuture(final long fileId) {
        CompletableFuture<SourcesServerResponse> future =
                new CompletableFuture<>();
        AsynchronousRequest.builder()
                .inetAddress(serverAddress)
                .query(new SourcesServerQuery(fileId))
                .responseReader(
                        AsynchronousRequest.defaultRead(
                                new SourcesServerResponse.Reader(),
                                futureHandler(future)
                        ))
                .onFailure(future::completeExceptionally)
                .build()
                .execute();
        return future;
    }

    @NotNull
    private CompletableFuture<StatResponseWithClientInfo> getStatRequest(@NotNull
                                                                         final ClientInfo clientInfo,
                                                                         final long fileId) {
        CompletableFuture<StatResponseWithClientInfo> future = new CompletableFuture<>();

        InetSocketAddress inetSocketAddress;
        try {
            inetSocketAddress = getAddressFromClientInfo(clientInfo);
        } catch (UnknownHostException e) {
            log.error("Unknown host exception!", e);
            future.completeExceptionally(e);
            return future;
        }

        AsynchronousRequest.builder()
                .inetAddress(inetSocketAddress)
                .query(new StatClientQuery(fileId))
                .responseReader(
                        AsynchronousRequest.defaultRead(
                                new StatClientResponse.Reader(),
                                new CompletionHandler<StatClientResponse, Void>() {
                                    @Override
                                    public void completed(@NotNull
                                                          final StatClientResponse result, Void attachment) {
                                        future.complete(new StatResponseWithClientInfo(result, clientInfo));
                                    }

                                    @Override
                                    public void failed(@NotNull
                                                       final Throwable exc, Void attachment) {
                                        future.completeExceptionally(exc);
                                    }
                                }
                        ))
                .onFailure(future::completeExceptionally)
                .build()
                .execute();
        return future;
    }

    @NotNull
    private InetSocketAddress getAddressFromClientInfo(@NotNull final ClientInfo clientInfo) throws UnknownHostException {
        return new InetSocketAddress(
                InetAddress.getByAddress(clientInfo.getAddress()),
                clientInfo.getPort()
        );
    }

    @Data
    private static class StatResponseWithClientInfo {
        @NotNull
        private final StatClientResponse statClientResponse;
        @NotNull
        private final ClientInfo clientInfo;
    }


}
