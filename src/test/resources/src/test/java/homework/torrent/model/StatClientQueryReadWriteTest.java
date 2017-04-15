package homework.torrent.model;

import homework.torrent.model.common.AbstractReaderWriterTest;
import org.junit.Test;

/**
 * Created by Dmitriy Baidin.
 */
public class StatClientQueryReadWriteTest {
    private AbstractReaderWriterTest<StatClientQuery> getReaderWriterTest() {
        return new AbstractReaderWriterTest<>(StatClientQuery.Reader::new, StatClientQuery::getWriter);
    }

    @Test
    public void simpleTestReadWrite() {
        getReaderWriterTest().test(new StatClientQuery(124));
    }
}
