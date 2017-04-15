package homework.torrent.model;

import homework.torrent.model.common.AbstractReaderWriterTest;
import org.junit.Test;

import java.util.Arrays;

/**
 * Created by Dmitriy Baidin.
 */
public class StatClientResponseReadWriteTest {

    private AbstractReaderWriterTest<StatClientResponse> getReaderWriterTest() {
        return new AbstractReaderWriterTest<>(StatClientResponse.Reader::new, StatClientResponse::getWriter);
    }

    @Test
    public void simpleTestReadWrite() {
        getReaderWriterTest().test(new StatClientResponse(Arrays.asList(1, 2, 3)));
    }

}
