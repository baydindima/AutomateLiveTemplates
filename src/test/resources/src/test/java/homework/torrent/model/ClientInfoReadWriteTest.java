package homework.torrent.model;

import homework.torrent.model.common.AbstractReaderWriterTest;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

/**
 * Created by Dmitriy Baidin.
 */
public class ClientInfoReadWriteTest {
    private AbstractReaderWriterTest<ClientInfo> getReaderWriterTest() {
        return new AbstractReaderWriterTest<>(ClientInfo.Reader::new, ClientInfo::getWriter);
    }

    @Test
    public void simpleTestReadWrite() {
        getReaderWriterTest().test(new ClientInfo(new @NotNull byte[]{127, 0, 0, 1}, (short) 1615));
    }

}
