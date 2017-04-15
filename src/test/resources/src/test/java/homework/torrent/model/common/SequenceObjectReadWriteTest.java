package homework.torrent.model.common;

import homework.torrent.model.reader.IntReader;
import homework.torrent.model.reader.LongReader;
import homework.torrent.model.reader.SequenceObjectReader;
import homework.torrent.model.reader.StringReader;
import homework.torrent.model.writer.IntWriter;
import homework.torrent.model.writer.LongWriter;
import homework.torrent.model.writer.SeqObjectWriter;
import homework.torrent.model.writer.StringWriter;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Dmitriy Baidin.
 */
public class SequenceObjectReadWriteTest {

    @Test
    public void singleObjectTestReadWrite() {
        IntReader intReader = new IntReader();
        final Integer value = 1024;
        AbstractReaderWriterTest<Object> test = new AbstractReaderWriterTest<Object>(
                () -> new SequenceObjectReader(intReader),
                o -> new SeqObjectWriter(new IntWriter(value))
        );
        test.readWrite(value, 2);
        assertEquals(value, intReader.getResult());
    }

    @Test
    public void seqObjectTestReadWrite() {
        IntReader intReader = new IntReader();
        StringReader stringReader = new StringReader();
        LongReader longReader = new LongReader();
        final Integer intValue = 1024;
        final String strValue = "val123";
        final Long longValue = 123L;
        AbstractReaderWriterTest<Object> test = new AbstractReaderWriterTest<Object>(
                () -> new SequenceObjectReader(intReader, stringReader, longReader),
                o -> new SeqObjectWriter(new IntWriter(intValue), new StringWriter(strValue), new LongWriter(longValue))
        );
        test.readWrite(intValue, 2);
        assertEquals(intValue, intReader.getResult());
        assertEquals(strValue, stringReader.getResult());
        assertEquals(longValue, longReader.getResult());
    }

}
