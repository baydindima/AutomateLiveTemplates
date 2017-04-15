package homework.torrent.model.common;

import homework.torrent.model.reader.IntReader;
import homework.torrent.model.writer.IntWriter;
import org.junit.Test;

/**
 * Tests of asynchronous read/write for integer
 */
public class IntReaderWriterTest {

    private AbstractReaderWriterTest<Integer> getReaderWriterTest() {
        return new AbstractReaderWriterTest<>(IntReader::new, IntWriter::new);
    }

    @Test
    public void zeroIntReadWrite() {
        int i = 0;
        getReaderWriterTest().test(i);
    }

    @Test
    public void bigIntReadWrite() {
        int i = 999999;
        getReaderWriterTest().test(i);
    }

    @Test
    public void negativeIntReadWrite() {
        int i = -999;
        getReaderWriterTest().test(i);
    }

}
