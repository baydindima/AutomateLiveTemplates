package homework.torrent.app;

import homework.torrent.model.FileInfo;
import homework.torrent.model.FilePart;
import org.jetbrains.annotations.NotNull;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static homework.torrent.app.TorrentClientStorageImpl.PART_SIZE;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Created by Dmitriy Baidin.
 */
public class TorrentClientStorageImplTest {
    @Rule
    @NotNull
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();
    @NotNull
    private final TorrentClientStorageImpl storage =
            new TorrentClientStorageImpl();

    @Test
    public void getStat() throws Exception {
        File file = temporaryFolder.newFile();
        long fileId1 = 10L;
        long fileId2 = 11L;

        assertThat(storage.getStat(fileId1), hasSize(0));


        storage.addExistingFile(file.toPath(),
                new FileInfo(fileId1, "name1", 100));
        assertThat(storage.getStat(fileId1), hasSize(1));


        storage.addExistingFile(file.toPath(),
                new FileInfo(fileId2, "name1", PART_SIZE * 10));
        assertThat(storage.getStat(fileId2), hasSize(10));

    }

    @Test
    public void getPart() throws Exception {
        File file = temporaryFolder.newFile();
        long fileId1 = 10L;

        FileInfo fileInfo = new FileInfo(fileId1, "name2", PART_SIZE * 3 + 1);

        storage.addNotLoadedFile(file.toPath(),
                fileInfo);
        FilePart part0 = storage.getPart(fileId1, 0);
        assertEquals(PART_SIZE, part0.getLength());
        assertEquals(0, part0.getOffset());
        assertEquals(file.toPath(), part0.getFilePath());

        FilePart part = storage.getPart(fileId1, 1);
        assertEquals(PART_SIZE, part.getLength());
        assertEquals(PART_SIZE, part.getOffset());
        assertEquals(file.toPath(), part.getFilePath());

        FilePart part2 = storage.getPart(fileId1, 3);
        assertEquals(1, part2.getLength());
        assertEquals(3 * PART_SIZE, part2.getOffset());
        assertEquals(file.toPath(), part2.getFilePath());
    }

    @Test
    public void getIncompleteParts() throws Exception {
        File file = temporaryFolder.newFile();
        long fileId1 = 10L;
        FileInfo fileInfo = new FileInfo(fileId1, "name2", PART_SIZE * 3);

        storage.addNotLoadedFile(file.toPath(),
                fileInfo);
        assertThat(storage.getIncompleteFileIds(),
                hasItem(fileId1));

        assertThat(storage.getIncompleteParts(fileId1),
                hasSize(3));
        storage.uploadPart(fileInfo, 1);
        assertThat(storage.getIncompleteParts(fileId1),
                hasSize(2));
        assertThat(storage.getFilesIdList(), hasSize(1));
        assertThat(storage.getStat(fileId1), hasSize(1));

        storage.uploadPart(fileInfo, 0);
        storage.uploadPart(fileInfo, 2);
        assertThat(storage.getIncompleteFileIds(),
                hasSize(0));
        assertThat(storage.getFilesIdList(), hasSize(1));
    }

}