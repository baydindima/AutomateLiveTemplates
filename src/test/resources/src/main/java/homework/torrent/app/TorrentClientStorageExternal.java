package homework.torrent.app;

import homework.torrent.exception.NoSuchFileException;
import homework.torrent.exception.NoSuchPartException;
import homework.torrent.model.FilePart;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Interface of torrent client for external usage.
 * For example, the {@link homework.torrent.network.server.ClientServer}
 * uses it to answer queries.
 *
 * @see TorrentClientStorageInternal
 * @see TorrentClientStorageImpl
 */
public interface TorrentClientStorageExternal {

    /**
     * Returns all available for loading numbers of parts of file.
     *
     * @param fileId file's id
     * @return all available for loading numbers of parts of file
     */
    @NotNull
    List<Integer> getStat(final long fileId);

    /**
     * Returns meta-info about part of file.
     *
     * @param fileId  file's id
     * @param partNum number of part in file
     * @return meta-info about part of file
     * @throws NoSuchPartException if no such part in file
     * @throws NoSuchFileException if no such file
     * @see FilePart
     */
    @NotNull
    FilePart getPart(final long fileId, final int partNum)
            throws NoSuchPartException, NoSuchFileException;

    /**
     * Returns all files' id which available for downloading
     * from this client (partly or completely).
     *
     * @return files' id which available for downloading from this client
     */
    @NotNull
    List<Long> getFilesIdList();

}
