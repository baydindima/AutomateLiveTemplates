package homework.torrent.model;

import homework.torrent.model.reader.*;
import homework.torrent.model.writer.LongWriter;
import homework.torrent.model.writer.ObjectWriter;
import homework.torrent.model.writer.SeqObjectWriter;
import homework.torrent.model.writer.StringWriter;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;


/**
 * Created by Dmitriy Baidin.
 */
@Data
public class FileInfo implements SerializableObject, Serializable {
    private final long id;
    @NotNull
    private final String name;
    private final long size;

    @NotNull
    @Override
    public ObjectWriter getWriter() {
        return new SeqObjectWriter(
                new LongWriter(id),
                new StringWriter(name),
                new LongWriter(size)
        );
    }

    public final static class Reader extends AbstractSingleReader<FileInfo> {
        @NotNull
        private final LongReader idReader = new LongReader();
        @NotNull
        private final StringReader nameReader = new StringReader();
        @NotNull
        private final LongReader sizeReader = new LongReader();
        @NotNull
        private final SequenceObjectReader seqReader = new SequenceObjectReader(idReader, nameReader, sizeReader);

        @NotNull
        @Override
        protected FileInfo calcResult() {
            return new FileInfo(
                    idReader.getResult(),
                    nameReader.getResult(),
                    sizeReader.getResult());
        }

        @NotNull
        @Override
        protected ObjectReader<?> getReader() {
            return seqReader;
        }
    }
}
