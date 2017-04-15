package homework.torrent.network.server;

import homework.torrent.app.TorrentTracker;
import homework.torrent.model.*;
import homework.torrent.model.reader.ObjectReader;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;


/**
 * Created by Dmitriy Baidin.
 */
public class TorrentServerTest {
    @Mock
    @NotNull
    private final TorrentTracker mockTracker = Mockito.mock(TorrentTracker.class);

    @Mock
    @NotNull
    private final AsynchronousSocketChannel mockSocket = Mockito.mock(AsynchronousSocketChannel.class);

    @Mock
    @NotNull
    private final InetAddress inetAddress = Mockito.mock(InetAddress.class);

    @NotNull
    private final TorrentServer torrentServer = new TorrentServer(mockTracker);


    @Test
    public void testListQuery() throws Exception {
        List<FileInfo> fileInfos = Arrays.asList(
                new FileInfo(1, "fileName1", 10),
                new FileInfo(2, "fileName2", 20)
        );
        Mockito.when(mockTracker.getFileList()).thenReturn(fileInfos);

        ListServerResponse.Reader reader = new ListServerResponse.Reader();

        setAnswer(reader);

        torrentServer.processMessage(new ListServerQuery(), inetAddress).accept(mockSocket);

        assertEquals(new ListServerResponse(fileInfos), reader.getResult());
        Mockito.verify(mockTracker, Mockito.times(1)).getFileList();
        Mockito.verify(mockSocket, Mockito.times(1)).write(any(), anyLong(), any(), any(), any());
    }


    @Test
    public void testUploadQuery() throws Exception {
        long fileId = 12L;
        String fileName = "fileName1";
        int fileSize = 1024;
        Mockito.when(mockTracker.uploadFile(fileName, fileSize)).thenReturn(fileId);

        UploadServerResponse.Reader reader = new UploadServerResponse.Reader();

        setAnswer(reader);

        torrentServer.processMessage(new UploadServerQuery(fileName, fileSize), inetAddress).accept(mockSocket);

        assertEquals(new UploadServerResponse(fileId), reader.getResult());
        Mockito.verify(mockTracker, Mockito.times(1)).uploadFile(fileName, fileSize);
        Mockito.verify(mockSocket, Mockito.times(1)).write(any(), anyLong(), any(), any(), any());
    }

    @Test
    public void testSourcesQuery() throws Exception {
        long fileId = 12L;
        List<ClientInfo> clientInfos = Arrays.asList(
                new ClientInfo(new byte[]{1, 2, 3, 4}, (short) 1024),
                new ClientInfo(new byte[]{1, 2, 3, 5}, (short) 1024)
        );
        Mockito.when(mockTracker.getSources(fileId)).thenReturn(clientInfos);

        SourcesServerResponse.Reader reader = new SourcesServerResponse.Reader();

        setAnswer(reader);

        torrentServer.processMessage(new SourcesServerQuery(fileId), inetAddress).accept(mockSocket);

        assertEquals(new SourcesServerResponse(clientInfos), reader.getResult());
        Mockito.verify(mockTracker, Mockito.times(1)).getSources(fileId);
        Mockito.verify(mockSocket, Mockito.times(1)).write(any(), anyLong(), any(), any(), any());
    }

    @Test
    public void testUpdateQuery() throws Exception {
        ClientInfo clientInfo = new ClientInfo(new byte[]{0, 0, 0, 0}, (short) 8888);
        List<Long> fileIds = Arrays.asList(1L, 2L);

        UpdateServerResponse.Reader reader = new UpdateServerResponse.Reader();

        setAnswer(reader);

        torrentServer.processMessage(new UpdateServerQuery((short) 8888, fileIds),
                InetAddress.getByAddress(new byte[]{0, 0, 0, 0})).accept(mockSocket);

        assertEquals(new UpdateServerResponse(true), reader.getResult());
        Mockito.verify(mockTracker, Mockito.times(1)).updateClientInfo(clientInfo, fileIds);
        Mockito.verify(mockSocket, Mockito.times(1)).write(any(), anyLong(), any(), any(), any());
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
