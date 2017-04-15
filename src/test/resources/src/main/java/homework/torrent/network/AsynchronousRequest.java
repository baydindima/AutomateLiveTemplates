package homework.torrent.network;

import homework.torrent.model.FilePart;
import homework.torrent.model.SerializableObject;
import homework.torrent.model.reader.AsynchronousFileReader;
import homework.torrent.model.reader.ObjectReader;
import homework.torrent.model.writer.ObjectWriter;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.function.Consumer;

/**
 * Asynchronous request.
 */
@Builder
@Slf4j
public class AsynchronousRequest<Query extends SerializableObject> {
    /**
     * Size of buffer in bytes.
     */
    private static final int BUFFER_SIZE = 1024;
    @NotNull
    private final InetSocketAddress inetAddress;
    @NotNull
    private final Query query;
    @NotNull
    private final Consumer<Throwable> onFailure;
    @NotNull
    private final Consumer<RequestAttachment> responseReader;
    @NotNull
    private final CompletionHandler<Integer, RequestAttachmentWithWriter> afterSocketWrite =
            new CompletionHandler<Integer, RequestAttachmentWithWriter>() {
                @Override
                public void completed(@NotNull final Integer result,
                                      @NotNull final RequestAttachmentWithWriter attachment) {
                    log.debug("Socket writing to {} completed with result: {}", inetAddress, result);
                    if (!attachment.getQueryWriter().isComplete()) {
                        attachment.getBuffer().clear();
                        attachment.getQueryWriter().write(attachment.getBuffer());
                        attachment.getBuffer().flip();
                        attachment.getSocketChannel().write(attachment.getBuffer(), attachment, afterSocketWrite);
                    } else {
                        responseReader.accept(new RequestAttachment(
                                attachment.getBuffer(),
                                attachment.getSocketChannel(),
                                inetAddress)
                        );
                    }
                }

                @Override
                public void failed(@NotNull final Throwable exc,
                                   @NotNull final RequestAttachmentWithWriter attachment) {
                    log.error("Error while request", exc);
                    closeConnection(attachment.getSocketChannel());
                    onFailure.accept(exc);
                }
            };

    @NotNull
    public static <T> Consumer<RequestAttachment> defaultRead(@NotNull
                                                              final ObjectReader<T> reader,
                                                              @NotNull
                                                              final CompletionHandler<T, Void> completionHandler) {
        return requestAttachment -> {
            log.info("Start receiving message from {}", requestAttachment.getInetAddress());
            requestAttachment.getBuffer().clear();

            requestAttachment.getSocketChannel().read(requestAttachment.getBuffer(), null,
                    new CompletionHandler<Integer, Void>() {
                        @Override
                        public void completed(@NotNull final Integer result, @Nullable final Void v) {
                            log.debug("Reading from socket {} with result: {}",
                                    requestAttachment.getInetAddress(),
                                    result
                            );
                            requestAttachment.getBuffer().flip();
                            reader.read(requestAttachment.getBuffer());
                            if (reader.isReady()) {
                                completionHandler.completed(reader.getResult(), null);
                            } else {
                                requestAttachment.getBuffer().clear();
                                requestAttachment.getSocketChannel()
                                        .read(
                                                requestAttachment.getBuffer(),
                                                null,
                                                this
                                        );
                            }
                        }

                        @Override
                        public void failed(@NotNull final Throwable exc,
                                           @Nullable final Void v) {
                            log.error("Error while request", exc);
                            completionHandler.failed(exc, v);
                        }
                    });
        };
    }

    @NotNull
    public static Consumer<RequestAttachment> filePartRead(@NotNull
                                                           final FilePart filePart,
                                                           @NotNull
                                                           final CompletionHandler<Void, Void> completionHandler) {
        return requestAttachment ->
                AsynchronousFileReader.builder()
                        .socketChannel(requestAttachment.getSocketChannel())
                        .filePath(filePart.getFilePath())
                        .length(filePart.getLength())
                        .offset(filePart.getOffset())
                        .completionHandler(completionHandler)
                        .build()
                        .read();
    }

    public void execute() {
        try {
            AsynchronousSocketChannel socketChannel = AsynchronousSocketChannel.open();
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            ObjectWriter queryWriter = query.getWriter();
            queryWriter.write(buffer);
            buffer.flip();
            log.info("Connecting with {}", inetAddress);
            socketChannel.connect(inetAddress,
                    new RequestAttachmentWithWriter(buffer, socketChannel, queryWriter),
                    new CompletionHandler<Void, RequestAttachmentWithWriter>() {
                        @Override
                        public void completed(final Void result,
                                              @NotNull final RequestAttachmentWithWriter attachment) {
                            log.info("Start sending message to {}", inetAddress);
                            attachment.getSocketChannel().write(
                                    attachment.getBuffer(),
                                    attachment,
                                    afterSocketWrite
                            );
                        }

                        @Override
                        public void failed(@NotNull final Throwable exc,
                                           @NotNull final RequestAttachmentWithWriter attachment) {
                            log.error("Error while request", exc);
                            closeConnection(attachment.getSocketChannel());
                            onFailure.accept(exc);
                        }
                    });
        } catch (IOException e) {
            onFailure.accept(e);
        }
    }

    private void closeConnection(@NotNull
                                 final AsynchronousSocketChannel socketChannel) {
        log.info("Close connection with {}", inetAddress);
        try {
            socketChannel.close();
        } catch (IOException e) {
            log.error("Error during closing of socket.", e);
        }
    }


    @Data
    public static class RequestAttachment {
        @NotNull
        private final ByteBuffer buffer;
        @NotNull
        private final AsynchronousSocketChannel socketChannel;
        @NotNull
        private final InetSocketAddress inetAddress;
    }

    @Data
    private static class RequestAttachmentWithWriter {
        @NotNull
        private final ByteBuffer buffer;
        @NotNull
        private final AsynchronousSocketChannel socketChannel;
        @NotNull
        private final ObjectWriter queryWriter;
    }

}
