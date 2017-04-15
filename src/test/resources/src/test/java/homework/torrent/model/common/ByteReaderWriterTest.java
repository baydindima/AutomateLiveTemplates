package homework.torrent.model.common;

import homework.torrent.model.reader.ByteReader;
import homework.torrent.model.writer.ByteWriter;
import org.junit.Test;

/**
 * Created by Dmitriy Baidin.
 */
public class ByteReaderWriterTest {

    private AbstractReaderWriterTest<Byte> getReaderWriterTest() {
        return new AbstractReaderWriterTest<>(ByteReader::new, ByteWriter::new);
    }

    @Test
    public void zeroReadWrite() {
        getReaderWriterTest().test((byte) 0);
    }

    @Test
    public void maxReadWrite() {
        getReaderWriterTest().test(Byte.MAX_VALUE);
    }

    @Test
    public void minReadWrite() {
        getReaderWriterTest().test(Byte.MIN_VALUE);
    }
}
