package homework.torrent.model.common;

import homework.torrent.model.reader.ShortReader;
import homework.torrent.model.writer.ShortWriter;
import org.junit.Test;

/**
 * Created by Dmitriy Baidin.
 */
public class ShortReaderWriterTest {

    private AbstractReaderWriterTest<Short> getReaderWriterTest() {
        return new AbstractReaderWriterTest<>(ShortReader::new, ShortWriter::new);
    }

    @Test
    public void zeroReadWrite() {
        getReaderWriterTest().test((short) 0);
    }

    @Test
    public void maxReadWrite() {
        getReaderWriterTest().test(Short.MAX_VALUE);
    }

    @Test
    public void minReadWrite() {
        getReaderWriterTest().test(Short.MIN_VALUE);
    }

}
