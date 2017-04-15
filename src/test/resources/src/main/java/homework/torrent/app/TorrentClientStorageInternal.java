package homework.torrent.app;

import homework.torrent.exception.NoSuchFileException;
import homework.torrent.exception.NoSuchPartException;
import homework.torrent.model.FileInfo;
import homework.torrent.model.FilePart;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;

/**
 * Interface of torrent client for internal usage.
 *
 * @see TorrentClientStorageExternal
 * @see TorrentClientStorageImpl
 */
public interface TorrentClientStorageInternal {

    /**
     * Returns all files' id which has incompletely not loaded parts.
     *
     * @return files' id which has incompletely not loaded parts
     */
    @NotNull
    List<Long> getIncompleteFileIds();

    /**
     * Returns all numbers of parts of file which isn't loaded.
     *
     * @param fileId id of file
     * @return numbers of parts which isn't loaded
     */
    @NotNull
    List<Integer> getIncompleteParts(long fileId);

    /**
     * Mark part of file as loaded.
     * If it doesn't have such part or file will not throw any exceptions.
     * Warning! no more than one thread can load one part.
     *
     * @param fileInfo file's meta-info
     * @param partNum  number of part in file
     * @see FileInfo
     */
    void uploadPart(@NotNull final FileInfo fileInfo, final int partNum);

    /**
     * Add not loaded file.
     * All parts of file will be mark as not loaded.
     *
     * @param filePath path to file.
     * @param fileInfo file's meta-info
     * @see FileInfo
     */
    void addNotLoadedFile(@NotNull final Path filePath,
                          @NotNull final FileInfo fileInfo);

    /**
     * Add file already existing on disk.
     * All parts of file will be mark as loaded.
     *
     * @param filePath path to file.
     * @param fileInfo file's meta-info.
     * @see FileInfo
     */
    void addExistingFile(@NotNull final Path filePath,
                         @NotNull final FileInfo fileInfo);

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
     * Check that file already contains in completed or uncompleted files.
     *
     * @param fileId id of file
     * @return true if file already in registered in tracker, false otherwise.
     */
    boolean contains(final long fileId);

}
