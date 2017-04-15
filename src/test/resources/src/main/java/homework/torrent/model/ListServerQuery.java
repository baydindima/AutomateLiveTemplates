package homework.torrent.model;


import homework.torrent.exception.InvalidQueryFormat;
import homework.torrent.model.reader.AbstractSingleReader;
import homework.torrent.model.reader.ByteReader;
import homework.torrent.model.reader.ObjectReader;
import homework.torrent.model.writer.ByteWriter;
import homework.torrent.model.writer.ObjectWriter;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

/**
 * List query.
 */
@Data
public class ListServerQuery implements TorrentServerQuery, SerializableObject {
    @Override
    public @NotNull TorrentServerQuery.Type getType() {
        return Type.List;
    }


    /**
     * Writer of get query.
     */
    @NotNull
    @Override
    public ObjectWriter getWriter() {
        return new ByteWriter(Type.List.getId());
    }

    /**
     * Reader of list query.
     */
    public static class Reader extends AbstractSingleReader<ListServerQuery> {
        @NotNull
        private final ByteReader typeReader = new ByteReader();

        @NotNull
        @Override
        protected ListServerQuery calcResult() {
            if (typeReader.getResult() != Type.List.getId()) {
                throw new InvalidQueryFormat(
                        String.format("Expected upload list, but got %s",
                                typeReader.getResult()));
            }
            return new ListServerQuery();
        }

        @NotNull
        @Override
        protected ObjectReader<?> getReader() {
            return typeReader;
        }
    }

}
