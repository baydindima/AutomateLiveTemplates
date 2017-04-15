package homework.torrent.model;

import homework.torrent.model.common.AbstractReaderWriterTest;
import org.junit.Test;

/**
 * Created by Dmitriy Baidin.
 */
public class FileInfoReadWriteTest {

    private AbstractReaderWriterTest<FileInfo> getReaderWriterTest() {
        return new AbstractReaderWriterTest<>(FileInfo.Reader::new, FileInfo::getWriter);
    }

    @Test
    public void simpleTestReadWrite() {
        getReaderWriterTest().test(new FileInfo(123, "name1", 400));
    }
}
