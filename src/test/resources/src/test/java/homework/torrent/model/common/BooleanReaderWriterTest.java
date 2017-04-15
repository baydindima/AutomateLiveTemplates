package homework.torrent.model.common;

import homework.torrent.model.reader.BooleanReader;
import homework.torrent.model.writer.BooleanWriter;
import org.junit.Test;

/**
 * Tests of asynchronous read/write for boolean
 */
public class BooleanReaderWriterTest {

    private AbstractReaderWriterTest<Boolean> getReaderWriterTest() {
        return new AbstractReaderWriterTest<>(BooleanReader::new, BooleanWriter::new);
    }

    @Test
    public void trueReadWrite() {
        getReaderWriterTest().test(true);
    }

    @Test
    public void falseReadWrite() {
        getReaderWriterTest().test(false);
    }
}
