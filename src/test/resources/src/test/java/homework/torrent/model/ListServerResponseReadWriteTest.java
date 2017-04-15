package homework.torrent.model;

import homework.torrent.model.common.AbstractReaderWriterTest;
import org.junit.Test;

import java.util.Arrays;

/**
 * Created by Dmitriy Baidin.
 */
public class ListServerResponseReadWriteTest {
    private AbstractReaderWriterTest<ListServerResponse> getReaderWriterTest() {
        return new AbstractReaderWriterTest<>(ListServerResponse.Reader::new, ListServerResponse::getWriter);
    }

    @Test
    public void simpleTestReadWrite() {
        getReaderWriterTest().test(new ListServerResponse(Arrays.asList(
                new FileInfo(123, "name1", 15),
                new FileInfo(124, "name2", 16))
        ));
    }

}
