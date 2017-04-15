package homework.torrent.model;

import homework.torrent.model.common.AbstractReaderWriterTest;
import org.junit.Test;

import java.util.Arrays;

/**
 * Created by Dmitriy Baidin.
 */
public class SourcesServerResponseReadWriteTest {
    private AbstractReaderWriterTest<SourcesServerResponse> getReaderWriterTest() {
        return new AbstractReaderWriterTest<>(SourcesServerResponse.Reader::new, SourcesServerResponse::getWriter);
    }

    @Test
    public void simpleTestReadWrite() {
        getReaderWriterTest().test(new SourcesServerResponse(
                Arrays.asList(
                        new ClientInfo(new byte[]{127, 0, 0, 1}, (short) 1222),
                        new ClientInfo(new byte[]{127, 0, 0, 1}, (short) 1222)
                )
        ));
    }
}
