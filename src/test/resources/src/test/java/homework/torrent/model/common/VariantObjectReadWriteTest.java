package homework.torrent.model.common;

import homework.torrent.model.*;
import homework.torrent.model.reader.VariantObjectReader;
import org.junit.Test;

import java.util.Arrays;

/**
 * Created by Dmitriy Baidin.
 */
public class VariantObjectReadWriteTest {

    @Test
    public void singleVariantObjectTestReadWrite() {
        AbstractReaderWriterTest<TorrentServerQuery> test = new AbstractReaderWriterTest<>(
                () -> new VariantObjectReader<>(1,
                        new VariantObjectReader.ReaderVariant<>(
                                new ListServerQuery.Reader(),
                                byteBuffer -> byteBuffer.array()[0] == TorrentServerQuery.Type.List.getId())),
                torrentServerQuery -> ((ListServerQuery) torrentServerQuery).getWriter()
        );
        test.test(new ListServerQuery());
    }

    @Test
    public void manyVariantObjectTestReadWrite() {
        AbstractReaderWriterTest<TorrentServerQuery> test = new AbstractReaderWriterTest<>(
                () -> new VariantObjectReader<>(1,
                        new VariantObjectReader.ReaderVariant<>(
                                new ListServerQuery.Reader(),
                                byteBuffer -> byteBuffer.array()[0] == TorrentServerQuery.Type.List.getId()),
                        new VariantObjectReader.ReaderVariant<>(
                                new UpdateServerQuery.Reader(),
                                byteBuffer -> byteBuffer.array()[0] == TorrentServerQuery.Type.Update.getId()),
                        new VariantObjectReader.ReaderVariant<>(
                                new SourcesServerQuery.Reader(),
                                byteBuffer -> byteBuffer.array()[0] == TorrentServerQuery.Type.Source.getId()),
                        new VariantObjectReader.ReaderVariant<>(
                                new UploadServerQuery.Reader(),
                                byteBuffer -> byteBuffer.array()[0] == TorrentServerQuery.Type.Upload.getId())
                ),
                torrentServerQuery -> {
                    if (torrentServerQuery instanceof ListServerQuery) {
                        return ((ListServerQuery) torrentServerQuery).getWriter();
                    }
                    if (torrentServerQuery instanceof UpdateServerQuery) {
                        return ((UpdateServerQuery) torrentServerQuery).getWriter();
                    }
                    if (torrentServerQuery instanceof SourcesServerQuery) {
                        return ((SourcesServerQuery) torrentServerQuery).getWriter();
                    }
                    return ((UploadServerQuery) torrentServerQuery).getWriter();
                }
        );
        test.test(new ListServerQuery());
        test.test(new UpdateServerQuery((short) 1024, Arrays.asList(1L, 2L, 4L)));
        test.test(new SourcesServerQuery(1025));
        test.test(new UploadServerQuery("1234", 1024L * 1024));
    }

}
