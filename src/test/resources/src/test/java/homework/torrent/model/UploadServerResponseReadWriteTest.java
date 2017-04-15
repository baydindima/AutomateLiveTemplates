package homework.torrent.model;

import homework.torrent.model.common.AbstractReaderWriterTest;
import org.junit.Test;

/**
 * Created by Dmitriy Baidin.
 */
public class UploadServerResponseReadWriteTest {

    private AbstractReaderWriterTest<UploadServerResponse> getReaderWriterTest() {
        return new AbstractReaderWriterTest<>(UploadServerResponse.Reader::new, UploadServerResponse::getWriter);
    }

    @Test
    public void simpleTestReadWrite() {
        getReaderWriterTest().test(new UploadServerResponse(1024));
    }

}
