package homework.torrent.model;

import homework.torrent.model.common.AbstractReaderWriterTest;
import org.junit.Test;

/**
 * Created by Dmitriy Baidin.
 */
public class GetClientQueryReadWriteTest {

    private AbstractReaderWriterTest<GetClientQuery> getReaderWriterTest() {
        return new AbstractReaderWriterTest<>(GetClientQuery.Reader::new, GetClientQuery::getWriter);
    }

    @Test
    public void simpleTestReadWrite() {
        getReaderWriterTest().test(new GetClientQuery(123, 12));
    }
}
