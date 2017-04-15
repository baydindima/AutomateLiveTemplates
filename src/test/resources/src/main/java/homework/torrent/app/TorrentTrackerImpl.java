package homework.torrent.app;

import homework.torrent.model.ClientInfo;
import homework.torrent.model.FileInfo;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implementation of torrent tracker.
 * This class store information about all files available in torrent's network
 * and available clients files.
 */
public class TorrentTrackerImpl implements TorrentTracker, Serializable {
    /**
     * Period of expiration of data about available files on a client.
     */
    private final static long EXPIRED_PERIOD = 1000 * 60 * 5; //5 min

    @NotNull
    private final static String DATA_FILE_NAME = "server_data.db";
    /**
     * Next free id for file.
     */
    @NotNull
    private final AtomicLong nextFreeId = new AtomicLong(0);
    /**
     * Map that stores file's id to map with clients,
     * who have that file to last update time.
     */
    @NotNull
    private final ConcurrentMap<Long, ConcurrentMap<ClientInfo, Long>>
            clients = new ConcurrentHashMap<>();
    /**
     * Queue of all files in torrent's network.
     */
    @NotNull
    private final Queue<FileInfo> files = new ConcurrentLinkedQueue<>();

    public static TorrentTrackerImpl createOrGetFromDisk() throws IOException, ClassNotFoundException {
        File dataFile = new File(DATA_FILE_NAME);
        if (dataFile.exists()) {
            try (FileInputStream fileInputStream = new FileInputStream(dataFile);
                 ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)
            ) {
                return (TorrentTrackerImpl) objectInputStream.readObject();
            }
        } else {
            return new TorrentTrackerImpl();
        }
    }

    /**
     * Save state of storage to disk.
     */
    public void saveOnDisk() throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(DATA_FILE_NAME);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)
        ) {
            objectOutputStream.writeObject(this);
        }
    }

    /**
     * Returns meta-info about all files known on the server.
     *
     * @return meta-info about all files
     * @see FileInfo
     */
    @NotNull
    @Override
    public List<FileInfo> getFileList() {
        return new ArrayList<>(files);
    }

    /**
     * Add info about new file
     *
     * @param fileName name of new file
     * @param fileSize size of new file
     * @return id of new file
     */
    @Override
    public long uploadFile(@NotNull final String fileName, final long fileSize) {
        long newFileId = nextFreeId.getAndIncrement();
        files.add(new FileInfo(newFileId, fileName, fileSize));
        return newFileId;
    }

    /**
     * Returns all known clients which have that file.
     *
     * @param fileId id of file
     * @return meta-info about known clients which have that file
     * @see ClientInfo
     */
    @NotNull
    @Override
    public List<ClientInfo> getSources(long fileId) {
        List<ClientInfo> result = new ArrayList<>();

        ConcurrentMap<ClientInfo, Long> clientsWithUpdateTime =
                clients.get(fileId);
        if (clientsWithUpdateTime != null) {
            long startTime = System.currentTimeMillis();
            Iterator<Map.Entry<ClientInfo, Long>> iterator =
                    clientsWithUpdateTime.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<ClientInfo, Long> next = iterator.next();
                if (next.getValue() + EXPIRED_PERIOD < startTime) {
                    iterator.remove();
                } else {
                    result.add(next.getKey());
                }
            }
        }

        return result;
    }

    /**
     * Update info about files available to the client.
     *
     * @param clientInfo meta-info of client
     * @param fileIds    id of available files
     * @see ClientInfo
     */
    @Override
    public void updateClientInfo(@NotNull final ClientInfo clientInfo,
                                 @NotNull final List<Long> fileIds) {
        long startTime = System.currentTimeMillis();
        fileIds.parallelStream().forEach(fileId ->
                clients.compute(fileId, (key, map) -> {
                            if (map == null) {
                                ConcurrentMap<ClientInfo, Long> newMap =
                                        new ConcurrentHashMap<>();
                                newMap.put(clientInfo, startTime);
                                return newMap;
                            } else {
                                map.compute(clientInfo, (info, lastUpdate) -> {
                                    if (lastUpdate == null
                                            || lastUpdate < startTime) {
                                        return startTime;
                                    } else {
                                        return lastUpdate;
                                    }
                                });
                                return map;
                            }
                        }
                ));
    }

}
