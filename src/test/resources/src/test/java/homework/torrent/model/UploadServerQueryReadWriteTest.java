package homework.torrent.model;

import homework.torrent.model.common.AbstractReaderWriterTest;
import org.junit.Test;

/**
 * Created by Dmitriy Baidin.
 */
public class UploadServerQueryReadWriteTest {

    private AbstractReaderWriterTest<UploadServerQuery> getReaderWriterTest() {
        return new AbstractReaderWriterTest<>(UploadServerQuery.Reader::new, UploadServerQuery::getWriter);
    }

    @Test
    public void simpleTestReadWrite() {
        getReaderWriterTest().test(new UploadServerQuery("FileName2", 1024 * 1024));
    }

}
