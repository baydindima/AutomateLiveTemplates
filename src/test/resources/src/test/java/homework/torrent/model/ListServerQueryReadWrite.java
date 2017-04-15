package homework.torrent.model;

import homework.torrent.model.common.AbstractReaderWriterTest;
import org.junit.Test;

/**
 * Created by Dmitriy Baidin.
 */
public class ListServerQueryReadWrite {

    private AbstractReaderWriterTest<ListServerQuery> getReaderWriterTest() {
        return new AbstractReaderWriterTest<>(ListServerQuery.Reader::new, ListServerQuery::getWriter);
    }

    @Test
    public void simpleTestReadWrite() {
        getReaderWriterTest().test(new ListServerQuery());
    }

}
