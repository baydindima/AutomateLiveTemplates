package homework.torrent.model;

import homework.torrent.model.common.AbstractReaderWriterTest;
import org.junit.Test;

/**
 * Created by Dmitriy Baidin.
 */
public class SourcesServerQueryReadWriteTest {
    private AbstractReaderWriterTest<SourcesServerQuery> getReaderWriterTest() {
        return new AbstractReaderWriterTest<>(SourcesServerQuery.Reader::new, SourcesServerQuery::getWriter);
    }

    @Test
    public void simpleTestReadWrite() {
        getReaderWriterTest().test(new SourcesServerQuery(144));
    }

}
