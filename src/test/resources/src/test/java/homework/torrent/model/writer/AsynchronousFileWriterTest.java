package homework.torrent.model.writer;

import org.jetbrains.annotations.NotNull;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileWriter;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

/**
 * Created by Dmitriy Baidin.
 */
public class AsynchronousFileWriterTest {

    @NotNull
    private final ByteBuffer result = ByteBuffer.allocate(4 * 1024);

    @Mock
    @NotNull
    private final AsynchronousSocketChannel mockSocket = Mockito.mock(AsynchronousSocketChannel.class,
            invocation -> {
                if (invocation.getMethod().getName().equals("write")) {
                    synchronized (result) {
                        result.put((ByteBuffer) invocation.getArgument(0));
                        result.notifyAll();
                    }
                    return null;
                }
                // Delegate to the default answer.
                return Mockito.RETURNS_DEFAULTS.answer(invocation);
            });

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void write() throws Exception {
        File file = temporaryFolder.newFile("file1024");
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write("Hello world!");
        fileWriter.close();


        AsynchronousFileWriter.builder()
                .filePath(Paths.get(file.getAbsolutePath()))
                .length(5)
                .offset(0)
                .outputChannel(mockSocket)
                .build()
                .write();

        synchronized (result) {
            result.wait(1000);
        }

        result.flip();
        byte[] byteResult = new byte[result.remaining()];
        result.get(byteResult);
        assertEquals("Hello", new String(byteResult));

        result.clear();

        AsynchronousFileWriter.builder()
                .filePath(Paths.get(file.getAbsolutePath()))
                .length(6)
                .offset(5)
                .outputChannel(mockSocket)
                .build()
                .write();

        synchronized (result) {
            result.wait(1000);
        }

        result.flip();
        byte[] byteResult2 = new byte[result.remaining()];
        result.get(byteResult2);
        assertEquals(" world", new String(byteResult2));
    }

}