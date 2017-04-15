package homework.torrent.app;

import homework.torrent.model.ClientInfo;
import homework.torrent.model.FileInfo;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Interface for TorrentTracker.
 * Used by torrent server.
 *
 * @see homework.torrent.network.server.TorrentServer
 * @see TorrentTrackerImpl
 */
public interface TorrentTracker {

    /**
     * Returns meta-info about all files known on the server.
     *
     * @return meta-info about all files
     * @see FileInfo
     */
    @NotNull
    List<FileInfo> getFileList();

    /**
     * Add info about new file
     *
     * @param fileName name of new file
     * @param fileSize size of new file
     * @return id of new file
     */
    long uploadFile(@NotNull final String fileName, final long fileSize);

    /**
     * Returns all known clients which have that file.
     *
     * @param fileId id of file
     * @return meta-info about known clients which have that file
     * @see ClientInfo
     */
    @NotNull
    List<ClientInfo> getSources(final long fileId);

    /**
     * Update info about files available to the client.
     *
     * @param clientInfo meta-info of client
     * @param fileIds    id of available files
     * @see ClientInfo
     */
    void updateClientInfo(@NotNull final ClientInfo clientInfo,
                          @NotNull final List<Long> fileIds);

}
