package homework.torrent.network.server;


import homework.torrent.exception.InvalidServerState;
import homework.torrent.model.reader.ObjectReader;
import homework.torrent.model.writer.ObjectWriter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Abstract asynchronous server.
 *
 * @param <T> expected query type
 */
@Slf4j
public abstract class Server<T> implements Closeable {
    /**
     * Default buffer size.
     */
    static final int BUFFER_SIZE = 1024;

    /**
     * Default waiting parameter.
     */
    private static final long TIMEOUT = 3000;

    /**
     * Port for listening.
     */
    @Getter
    private final int port;

    /**
     * True if already started, false otherwise.
     */
    @Getter
    private boolean isStarted;
    /**
     * True if already closed, false otherwise.
     */
    @Getter
    private boolean isClosed;

    /**
     * An asynchronous channel for stream-oriented listening sockets.
     */
    private AsynchronousServerSocketChannel serverSocketChannel;

    /**
     * Create new instance of server, that listening specified port.
     *
     * @param listeningPort port for listening
     */
    Server(final int listeningPort) {
        this.port = listeningPort;
    }


    /**
     * Start listening port.
     *
     * @throws IOException if IO exception occurs
     * @throws InvalidServerState if server was already start or close
     */
    public final void start() throws IOException, InvalidServerState {
        if (isStarted) {
            throw new InvalidServerState("Server already started!");
        }
        if (isClosed) {
            throw new InvalidServerState("Server already closed!");
        }
        isStarted = true;
        serverSocketChannel = AsynchronousServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(port));

        log.info("Server listening {}", port);

        serverSocketChannel.accept(null,
                new CompletionHandler<AsynchronousSocketChannel, Void>() {
                    @Override
                    public void completed(
                            @NotNull final AsynchronousSocketChannel client,
                            @Nullable final Void attachment) {
                        log.info("Connection established with client {}",
                                getRemoteAddress(client));
                        serverSocketChannel.accept(attachment, this);
                        readQuery(client);
                    }

                    @Override
                    public void failed(
                            @NotNull final Throwable exc,
                            @Nullable final Void attachment) {
                        log.error("Exception occurred while "
                                + "trying establish connection!", exc);
                    }
                });
    }

    /**
     * Read query from client.
     *
     * @param client client socket
     */
    private void readQuery(@NotNull final AsynchronousSocketChannel client) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        ObjectReader<T> queryReader = getReaderSupplier().get();
        client.read(byteBuffer,
                TIMEOUT,
                TimeUnit.MILLISECONDS,
                byteBuffer,
                new CompletionHandler<Integer, ByteBuffer>() {
                    @Override
                    public void completed(
                            @NotNull final Integer readCount,
                            @NotNull final ByteBuffer byteBuffer) {
                        log.debug("Read count: {}", readCount);
                        byteBuffer.flip();
                        queryReader.read(byteBuffer);
                        if (queryReader.isReady()) {
                            log.info("Get message: {}; from client: {}",
                                    queryReader.getResult(),
                                    getRemoteAddress(client));

                            processMessage(queryReader.getResult(),
                                    ((InetSocketAddress) getRemoteAddress(client)).getAddress())
                                    .accept(client);
                        } else {
                            byteBuffer.clear();
                            client.read(
                                    byteBuffer,
                                    TIMEOUT,
                                    TimeUnit.MILLISECONDS,
                                    byteBuffer,
                                    this);
                        }
                    }

                    @Override
                    public void failed(
                            @NotNull final Throwable exc,
                            @NotNull final ByteBuffer byteBuffer) {
                        log.error("Exception occurred while "
                                        + "processing message from {}!",
                                getRemoteAddress(client), exc);
                        closeConnection(client);
                    }
                });
    }

    /**
     * Close connection.
     *
     * @param client socket which will be closed
     */
    protected final void closeConnection(
            @NotNull final AsynchronousSocketChannel client) {
        log.info("Closing connection with client {}", getRemoteAddress(client));
        try {
            client.close();
        } catch (IOException e) {
            log.info("Exception while closing connection with client {}",
                    getRemoteAddress(client));
        }
    }

    /**
     * Abstract method for getting read supplier.
     * Inheriting classes should override this.
     *
     * @return factory of query reader.
     */
    @NotNull
    protected abstract Supplier<@NotNull ObjectReader<T>>
    getReaderSupplier();

    /**
     * Abstract method for processing query.
     * Inheriting classes should override this with logic of processing queries.
     *
     * @param query received query.
     * @return function which send result to client
     */
    @NotNull
    protected abstract Consumer<@NotNull AsynchronousSocketChannel>
    processMessage(@NotNull final T query, @NotNull final InetAddress inetAddress);

    /**
     * Default message sending to client.
     * Used when message is not very large and
     * it can be get in non blocking mode.
     *
     * @param writer message writer
     * @return function which send result to client
     */
    @NotNull
    protected final Consumer<@NotNull AsynchronousSocketChannel>
    defaultWrite(@NotNull final ObjectWriter writer) {
        return client -> {
            log.info("Start sending message to {}", getRemoteAddress(client));

            ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
            writer.write(byteBuffer);
            byteBuffer.flip();

            client.write(byteBuffer,
                    TIMEOUT,
                    TimeUnit.MILLISECONDS,
                    byteBuffer,
                    new CompletionHandler<Integer, ByteBuffer>() {
                        @Override
                        public void completed(
                                @NotNull final Integer writeCount,
                                @NotNull final ByteBuffer byteBuffer) {
                            log.debug("Write count: {}", writeCount);
                            if (writer.isComplete()) {
                                log.info("Send message to {}",
                                        getRemoteAddress(client));
                                closeConnection(client);
                            } else {
                                byteBuffer.clear();
                                writer.write(byteBuffer);
                                byteBuffer.flip();
                                client.write(byteBuffer,
                                        TIMEOUT,
                                        TimeUnit.MILLISECONDS,
                                        byteBuffer,
                                        this);
                            }
                        }

                        @Override
                        public void failed(
                                @NotNull final Throwable exc,
                                @NotNull final ByteBuffer attachment) {
                            log.error("Exception occurred while"
                                            + " sending message to {}!",
                                    getRemoteAddress(client), exc);
                            closeConnection(client);
                        }
                    });
        };
    }

    /**
     * Get address of remote client.
     *
     * @param client socket
     * @return socket address
     */
    @NotNull
    private SocketAddress getRemoteAddress(
            @NotNull final AsynchronousSocketChannel client) {
        try {
            return client.getRemoteAddress();
        } catch (IOException e) {
            log.error("Exception occurred while getting remote address", e);
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Stop listening port and close sockets.
     *
     * @throws IOException of IO exception occurs
     */
    public final void close() throws IOException {
        isClosed = true;
        if (serverSocketChannel != null) {
            serverSocketChannel.close();
        }
    }

}
