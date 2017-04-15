package homework.torrent.app;


import homework.torrent.exception.FileAlreadyContainsInStorage;
import homework.torrent.model.FileInfo;
import homework.torrent.model.ProgressListener;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Future;

/**
 * API for user's commands
 */
public interface TorrentClient {
    /**
     * Returns meta-info about all available files in torrent's network.
     *
     * @return meta-info about all available files
     */
    @NotNull
    Future<List<FileInfo>> getAvailableFiles();

    /**
     * Add file to torrent's network.
     * Send upload query to server and add info about this file to storage.
     *
     * @param filePath path of file
     * @return future of completion
     */
    @NotNull
    Future<FileInfo> addFileToTorrent(@NotNull final Path filePath) throws NoSuchFileException;

    /**
     * Download file.
     *
     * @param destinationPath path of file
     * @param fileInfo        info about file
     * @return future of loading
     * @throws IOException if IOException occurs
     */
    ProgressListener downloadFile(@NotNull final Path destinationPath, @NotNull final FileInfo fileInfo) throws IOException, FileAlreadyContainsInStorage;

}
