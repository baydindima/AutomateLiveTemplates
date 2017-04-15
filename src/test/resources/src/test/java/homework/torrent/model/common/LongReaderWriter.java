package homework.torrent.model.common;

import homework.torrent.model.reader.LongReader;
import homework.torrent.model.writer.LongWriter;
import org.junit.Test;

/**
 * Created by Dmitriy Baidin.
 */
public class LongReaderWriter {

    private AbstractReaderWriterTest<Long> getReaderWriterTest() {
        return new AbstractReaderWriterTest<>(LongReader::new, LongWriter::new);
    }

    @Test
    public void zeroIntReadWrite() {
        getReaderWriterTest().test(0L);
    }

    @Test
    public void bigIntReadWrite() {
        getReaderWriterTest().test(Long.MAX_VALUE);
    }

    @Test
    public void negativeIntReadWrite() {
        getReaderWriterTest().test(Long.MIN_VALUE);
    }

}
