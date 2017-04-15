package homework.torrent.model.reader;

import homework.torrent.model.ProgressListener;
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
 * Asynchronous file reader.
 * Reads file asynchronously.
 */
@Slf4j
@Builder
public class AsynchronousFileReader {
    private final static int BUFFER_SIZE = 4 * 1024;
    @NotNull
    private final AsynchronousSocketChannel socketChannel;
    @NotNull
    private final Path filePath;
    private final long offset;
    private final int length;
    @NotNull
    private final CompletionHandler<Void, Void> completionHandler;

    @NotNull
    private final ProgressListener progressListener;
    @NotNull
    private final CompletionHandler<Integer, Attachment> afterSocketRead = new CompletionHandler<Integer, Attachment>() {
        @Override
        public void completed(Integer result, Attachment attachment) {
            log.debug("Socket reading with result: {}", result);
            if (result != -1) {
                attachment.buffer.flip();
                attachment.fileChannel.write(attachment.buffer, attachment.position, attachment, afterFileWrite);
            } else {
                log.error("Can not read from socket!");
                closeChannel(socketChannel);
                closeChannel(attachment.fileChannel);
            }
        }

        @Override
        public void failed(Throwable exc, Attachment attachment) {
            log.error("Error while reading socket!", exc);
            closeChannel(socketChannel);
            closeChannel(attachment.fileChannel);
        }
    };
    @NotNull
    private final CompletionHandler<Integer, Attachment> afterFileWrite = new CompletionHandler<Integer, Attachment>() {

        @Override
        public void completed(Integer result, Attachment attachment) {
            log.debug("Writing to file with result: {}", result);
            if (result != -1) {
                attachment.position += result;
                progressListener.load(result);
                if (attachment.position < attachment.lastPosition) {
                    attachment.buffer.clear();
                    if (attachment.lastPosition - attachment.position < attachment.buffer.capacity()) {
                        attachment.buffer.limit((int) (attachment.lastPosition - attachment.position));
                    }
                    socketChannel.read(attachment.buffer, attachment, afterSocketRead);
                } else {
                    log.info("File transferring completed!");
                    closeChannel(socketChannel);
                    closeChannel(attachment.fileChannel);
                    completionHandler.completed(null, null);
                }
            } else {
                log.error("Can not write to file!");
                closeChannel(socketChannel);
                closeChannel(attachment.fileChannel);
            }

        }

        @Override
        public void failed(Throwable exc, Attachment attachment) {
            log.error("Error while writing to file!", exc);
            closeChannel(socketChannel);
            closeChannel(attachment.fileChannel);
        }
    };

    public void read() {
        int bufferSize = Math.min(length, BUFFER_SIZE);
        ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize);
        try {
            AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(filePath,
                    StandardOpenOption.WRITE);

            Attachment attachment = new Attachment(fileChannel, byteBuffer, offset + length, offset);
            socketChannel.read(attachment.buffer, attachment, afterSocketRead);
        } catch (IOException e) {
            log.error("Exception while opening file: {}",
                    filePath.toString(), e);
            closeChannel(socketChannel);
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
    private static class Attachment {
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
