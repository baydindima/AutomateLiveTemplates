package homework.torrent.model;

import homework.torrent.model.common.AbstractReaderWriterTest;
import org.junit.Test;

import java.util.Arrays;

/**
 * Created by Dmitriy Baidin.
 */
public class UpdateServerQueryReadWriteTest {

    private AbstractReaderWriterTest<UpdateServerQuery> getReaderWriterTest() {
        return new AbstractReaderWriterTest<>(UpdateServerQuery.Reader::new, UpdateServerQuery::getWriter);
    }

    @Test
    public void simpleTestReadWrite() {
        getReaderWriterTest().test(new UpdateServerQuery((short) 1234, Arrays.asList(1L, 2L, 3L, 4L)));
    }

}
