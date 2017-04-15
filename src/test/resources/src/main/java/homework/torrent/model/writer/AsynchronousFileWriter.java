package homework.torrent.model.writer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannel;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Created by Dmitriy Baidin.
 */
@Slf4j
@Builder
public class AsynchronousFileWriter {
    private final static int BUFFER_SIZE = 4 * 1024;
    @NotNull
    private final AsynchronousSocketChannel outputChannel;
    @NotNull
    private final Path filePath;
    private final long offset;
    private final int length;
    /**
     * Handler for processing after writing in socket.
     */
    @NotNull
    private final CompletionHandler<Integer, Attachment> afterSocketWrite =
            new CompletionHandler<Integer, Attachment>() {
                @Override
                public void completed(@NotNull final Integer result,
                                      @NotNull final Attachment attachment) {
                    log.debug("Socket writing with result: {}", result);
                    if (attachment.position >= attachment.lastPosition) {
                        attachment.buffer.clear();
                        if (attachment.lastPosition - attachment.position < attachment.buffer.capacity()) {
                            attachment.buffer.limit((int) (attachment.lastPosition - attachment.position));
                        }
                        attachment.fileChannel.read(attachment.buffer,
                                attachment.position, attachment, afterFileRead);
                    } else {
                        log.info("File transferring completed!");
                        closeChannel(outputChannel);
                        closeChannel(attachment.fileChannel);
                    }
                }

                @Override
                public void failed(@NotNull final Throwable exc,
                                   @NotNull final Attachment attachment) {
                    log.error("Error while socket writing!", exc);
                    closeChannel(outputChannel);
                    closeChannel(attachment.fileChannel);
                }
            };
    /**
     * Handler for processing after reading from file.
     */
    @NotNull
    private final CompletionHandler<Integer, Attachment> afterFileRead =
            new CompletionHandler<Integer, Attachment>() {
                @Override
                public void completed(@NotNull final Integer result,
                                      @NotNull final Attachment attachment) {
                    log.debug("File reading with result: {}", result);
                    if (result != -1) {
                        attachment.position += result;
                        attachment.buffer.flip();
                        outputChannel.write(
                                attachment.buffer,
                                attachment,
                                afterSocketWrite);
                    } else {
                        log.error("Can not read file!");
                        closeChannel(outputChannel);
                        closeChannel(attachment.fileChannel);
                    }
                }

                @Override
                public void failed(@NotNull final Throwable exc,
                                   @NotNull final Attachment attachment) {
                    log.error("Error while reading file!", exc);
                    closeChannel(outputChannel);
                    closeChannel(attachment.fileChannel);
                }
            };

    public void write() {
        int bufferSize = Math.min(length, BUFFER_SIZE);
        ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize);
        try {
            AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(filePath,
                    StandardOpenOption.READ);

            Attachment attachment = new Attachment(fileChannel, byteBuffer, offset + length, offset);
            attachment.fileChannel.read(attachment.buffer,
                    attachment.position, attachment, afterFileRead);
        } catch (IOException e) {
            log.error("Exception while opening file: {}",
                    filePath.toString(), e);
            closeChannel(outputChannel);
        }
    }

    private void closeChannel(AsynchronousChannel channel) {
        try {
            channel.close();
        } catch (IOException e) {
            log.info("Exception during closing channel.");
        }
    }

    @AllArgsConstructor
    static class Attachment {
        /**
         * File channel for reading.
         */
        @NotNull
        private final AsynchronousFileChannel fileChannel;
        /**
         * Buffer for data transfer.
         */
        @NotNull
        private final ByteBuffer buffer;
        /**
         * Last position for reading (excluding).
         */
        private final long lastPosition;
        /**
         * Current position in file.
         */
        private long position;
    }
}
