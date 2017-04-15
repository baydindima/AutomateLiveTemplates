package homework.torrent.network.server;

import homework.torrent.app.TorrentClientStorageExternal;
import homework.torrent.model.StatClientQuery;
import homework.torrent.model.StatClientResponse;
import homework.torrent.model.reader.ObjectReader;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

/**
 * Created by Dmitriy Baidin.
 */
public class ClientServerTest {

    @Mock
    @NotNull
    private final AsynchronousSocketChannel mockSocket =
            Mockito.mock(AsynchronousSocketChannel.class);

    @Mock
    @NotNull
    private final TorrentClientStorageExternal storageExternal =
            Mockito.mock(TorrentClientStorageExternal.class);

    @NotNull
    private final ClientServer clientServer =
            new ClientServer(1010, storageExternal);

    @Test
    public void statQuery() throws Exception {
        List<Integer> filePartList = Collections.singletonList(10);
        Mockito.when(storageExternal.getStat(10))
                .thenReturn(filePartList);
        StatClientResponse.Reader reader = new StatClientResponse.Reader();

        setAnswer(reader);

        clientServer.processMessage(
                new StatClientQuery(10),
                InetAddress.getByAddress(new byte[]{127, 1, 1, 0})
        ).accept(mockSocket);

        assertEquals(new StatClientResponse(filePartList), reader.getResult());

        Mockito.verify(storageExternal, Mockito.times(1)).getStat(10);
    }


    private void setAnswer(@NotNull final ObjectReader<?> reader) throws IOException {
        Mockito.when(mockSocket.getRemoteAddress()).thenReturn(new InetSocketAddress(
                InetAddress.getByAddress(new byte[]{0, 0, 0, 0}), 1024));
        Mockito.doAnswer(invocation -> {
            reader.read(invocation.getArgument(0));
            return null;
        }).when(mockSocket).write(any(), anyLong(), any(), any(), any());
    }
}