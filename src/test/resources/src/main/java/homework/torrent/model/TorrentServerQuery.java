package homework.torrent.model;

import homework.torrent.model.reader.ObjectReader;
import homework.torrent.model.reader.VariantObjectReader;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for server queries.
 */
public interface TorrentServerQuery {

    /**
     * General reader for server queries.
     *
     * @return general reader for server queries
     */
    static ObjectReader<TorrentServerQuery> getReader() {
        return new VariantObjectReader<>(1,
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
        List((byte) 1),
        Upload((byte) 2),
        Source((byte) 3),
        Update((byte) 4);
        @Getter
        private final byte id;

        Type(final byte value) {
            this.id = value;
        }
    }

}
