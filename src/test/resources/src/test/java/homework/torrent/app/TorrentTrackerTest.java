package homework.torrent.app;

import homework.torrent.model.ClientInfo;
import homework.torrent.model.FileInfo;
import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;

/**
 * Created by Dmitriy Baidin.
 */
public class TorrentTrackerTest {
    private TorrentTrackerImpl torrentTracker = new TorrentTrackerImpl();

    @Test
    public void getFileList() throws Exception {
        long fileId1 = torrentTracker.uploadFile("File1", 1024);
        long fileId2 = torrentTracker.uploadFile("File2", 1024);
        long fileId3 = torrentTracker.uploadFile("File3", 1024);
        long fileId4 = torrentTracker.uploadFile("File4", 1024);
        long fileId5 = torrentTracker.uploadFile("File5", 1024);

        FileInfo fileInfo1 = new FileInfo(fileId1, "File1", 1024);
        FileInfo fileInfo2 = new FileInfo(fileId2, "File2", 1024);
        FileInfo fileInfo3 = new FileInfo(fileId3, "File3", 1024);
        FileInfo fileInfo4 = new FileInfo(fileId4, "File4", 1024);
        FileInfo fileInfo5 = new FileInfo(fileId5, "File5", 1024);

        assertThat(torrentTracker.getFileList(), contains(
                fileInfo1,
                fileInfo2,
                fileInfo3,
                fileInfo4,
                fileInfo5
        ));
    }

    @Test
    public void uploadFile() throws Exception {
        final int threadCount = 10;
        final int uploadCount = 10;
        ConcurrentMap<Long, Integer> idMap = new ConcurrentHashMap<>();

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                for (int j = 0; j < uploadCount; j++) {
                    long id = torrentTracker.uploadFile("fileName", 10);
                    idMap.merge(id, 1, (c1, c2) -> c1 + c2);
                }
            });
        }
        executorService.shutdown();

        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

            assertThat(torrentTracker.getFileList(), hasSize(threadCount * uploadCount));

            for (Integer count : idMap.values()) {
                assertEquals((Integer) 1, count);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void getSources() throws Exception {
        long fileId1 = torrentTracker.uploadFile("File1", 1024);
        long fileId2 = torrentTracker.uploadFile("File2", 1024);
        long fileId3 = torrentTracker.uploadFile("File3", 1024);
        long fileId4 = torrentTracker.uploadFile("File4", 1024);
        long fileId5 = torrentTracker.uploadFile("File5", 1024);

        ClientInfo clientInfo = new ClientInfo(new byte[4], (short) 123);
        torrentTracker.updateClientInfo(clientInfo,
                Arrays.asList(fileId1, fileId2, fileId3, fileId4, fileId5));

        assertThat(torrentTracker.getSources(fileId1), contains(clientInfo));
        assertThat(torrentTracker.getSources(fileId2), contains(clientInfo));
        assertThat(torrentTracker.getSources(fileId3), contains(clientInfo));
        assertThat(torrentTracker.getSources(fileId4), contains(clientInfo));
        assertThat(torrentTracker.getSources(fileId5), contains(clientInfo));
    }

    @Test
    public void updateClientInfo() throws Exception {
        AtomicInteger threadNum = new AtomicInteger();

        long fileId1 = torrentTracker.uploadFile("File1", 1024);
        long fileId2 = torrentTracker.uploadFile("File2", 1024);
        long fileId3 = torrentTracker.uploadFile("File3", 1024);
        long fileId4 = torrentTracker.uploadFile("File4", 1024);
        long fileId5 = torrentTracker.uploadFile("File5", 1024);

        final int threadCount = 10;
        final int updateCount = 10;

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                ClientInfo clientInfo = new ClientInfo(new byte[]{0, 0, 0, (byte) threadNum.getAndIncrement()}, (short) 123);
                for (int j = 0; j < updateCount; j++) {
                    torrentTracker.updateClientInfo(clientInfo,
                            Arrays.asList(fileId1, fileId2, fileId3, fileId4, fileId5));
                }
            });
        }
        executorService.shutdown();

        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

            assertThat(torrentTracker.getSources(fileId1), hasSize(threadCount));
            assertThat(torrentTracker.getSources(fileId2), hasSize(threadCount));
            assertThat(torrentTracker.getSources(fileId3), hasSize(threadCount));
            assertThat(torrentTracker.getSources(fileId4), hasSize(threadCount));
            assertThat(torrentTracker.getSources(fileId5), hasSize(threadCount));

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
