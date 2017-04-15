package homework.torrent.model.reader;

import org.jetbrains.annotations.NotNull;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

/**
 * Created by Dmitriy Baidin.
 */
public class AsynchronousFileReaderTest {

    @Rule
    @NotNull
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Mock
    @NotNull
    private final AsynchronousSocketChannel mockSocket = Mockito.mock(AsynchronousSocketChannel.class,
            invocation -> {
                if (invocation.getMethod().getName().equals("read")) {
                    ByteBuffer argument = invocation.getArgument(0);
                    CompletionHandler<Integer, ?> completionHandler = invocation.getArgument(4);
                    argument.put("Hello".getBytes());
                    completionHandler.completed(5, invocation.getArgument(3));
                    return null;
                }
                // Delegate to the default answer.
                return Mockito.RETURNS_DEFAULTS.answer(invocation);
            });


    @Test
    public void read() throws Exception {
        final Object lock = new Object();
        int messageLength = 5;
        byte[] bytes = new byte[messageLength];

        File file = temporaryFolder.newFile("File123");
        RandomAccessFile rw = new RandomAccessFile(file, "rw");
        rw.setLength(messageLength);

        AsynchronousFileReader.builder()
                .length(messageLength)
                .offset(0)
                .filePath(Paths.get(file.getAbsolutePath()))
                .socketChannel(mockSocket)
                .completionHandler(new CompletionHandler<Void, Void>() {
                    @Override
                    public void completed(Void result, Void attachment) {
                        try {
                            rw.readFully(bytes);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            synchronized (lock) {
                                lock.notifyAll();
                            }
                        }

                    }

                    @Override
                    public void failed(Throwable exc, Void attachment) {
                        exc.printStackTrace();
                    }
                })
                .build()
                .read();

        synchronized (lock) {
            lock.wait(10000);
        }

        assertEquals("Hello", new String(bytes));
    }

}