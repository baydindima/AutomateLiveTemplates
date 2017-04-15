package homework.torrent.model;

import homework.torrent.model.reader.ObjectReader;
import homework.torrent.model.reader.VariantObjectReader;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for clients queries.
 */
public interface TorrentClientQuery {

    /**
     * General reader of clients queries.
     *
     * @return general reader of clients queries
     */
    static ObjectReader<TorrentClientQuery> getReader() {
        return new VariantObjectReader<>(1,
                new VariantObjectReader.ReaderVariant<>(
                        new GetClientQuery.Reader(),
                        byteBuffer -> byteBuffer.array()[0] == Type.Get.getId()),
                new VariantObjectReader.ReaderVariant<>(
                        new StatClientQuery.Reader(),
                        byteBuffer -> byteBuffer.array()[0] == Type.Stat.getId())
        );
    }

    /**
     * Type of query.
     */
    @NotNull Type getType();

    /**
     * Type of query.
     */
    enum Type {
        Stat((byte) 1),
        Get((byte) 2);
        @Getter
        private final byte id;

        Type(final byte value) {
            this.id = value;
        }
    }

}
