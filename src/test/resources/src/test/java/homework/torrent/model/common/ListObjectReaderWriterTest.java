package homework.torrent.model.common;

import homework.torrent.model.reader.IntReader;
import homework.torrent.model.reader.ListObjectReader;
import homework.torrent.model.reader.StringReader;
import homework.torrent.model.writer.IntWriter;
import homework.torrent.model.writer.ListObjectWriter;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Dmitriy Baidin.
 */
public class ListObjectReaderWriterTest {

    private AbstractReaderWriterTest<List<Integer>> getReaderWriterTest() {
        return new AbstractReaderWriterTest<>(
                () -> new ListObjectReader<>(IntReader::new),
                integers -> new ListObjectWriter<>(integers, IntWriter::new)
        );
    }


    @Test
    public void emptyReadWrite() {
        getReaderWriterTest().test(Collections.emptyList());
    }

    @Test
    public void singleReadWrite() {
        getReaderWriterTest().test(Collections.singletonList(10));
    }

    @Test
    public void threeReadWrite() {
        getReaderWriterTest().test(Arrays.asList(1, 2, 3));
    }

    private AbstractReaderWriterTest<List<String>> getReaderWriterStringTest() {
        return new AbstractReaderWriterTest<>(() -> new ListObjectReader<>(StringReader::new),
                strings -> new ListObjectWriter<>(strings, homework.torrent.model.writer.StringWriter::new)
        );
    }

    @Test
    public void stringReadWrite() {
        getReaderWriterStringTest().test(Arrays.asList("", "123 asdc", ""));
    }

}
