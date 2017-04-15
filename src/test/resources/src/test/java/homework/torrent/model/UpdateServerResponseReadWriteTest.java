package homework.torrent.model;

import homework.torrent.model.common.AbstractReaderWriterTest;
import org.junit.Test;

/**
 * Created by Dmitriy Baidin.
 */
public class UpdateServerResponseReadWriteTest {
    private AbstractReaderWriterTest<UpdateServerResponse> getReaderWriterTest() {
        return new AbstractReaderWriterTest<>(UpdateServerResponse.Reader::new, UpdateServerResponse::getWriter);
    }

    @Test
    public void simpleTestReadWrite() {
        getReaderWriterTest().test(new UpdateServerResponse(true));
    }

}
