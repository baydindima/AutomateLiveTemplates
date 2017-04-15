package homework.torrent.app;

import homework.torrent.exception.NoSuchFileException;
import homework.torrent.exception.NoSuchPartException;
import homework.torrent.model.FileInfo;
import homework.torrent.model.FilePart;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Implementation of torrent client.
 * This class stores information about all available files one the client.
 * And about all available parts of partly loaded files.
 *
 * @see homework.torrent.app.TorrentClientStorageExternal
 * @see homework.torrent.app.TorrentClientStorageInternal
 */
@Slf4j
public class TorrentClientStorageImpl
        implements TorrentClientStorageExternal, TorrentClientStorageInternal, Serializable {
    /**
     * Max size of file's part
     */
    final static int PART_SIZE = 1024 * 1024 * 10; // 10 Mb
    @NotNull
    private static final String DATA_FILE_NAME = "client_storage.db";
    /**
     * Map that stores file id to info about fully available files
     */
    @NotNull
    private final ConcurrentMap<Long, FileInfoWithPath> completedFiles =
            new ConcurrentHashMap<>();
    /**
     * Map that stores file if to info about partly available files
     */
    @NotNull
    private final ConcurrentMap<Long, IncompleteFileInfo> uncompletedFiles =
            new ConcurrentHashMap<>();

    public static TorrentClientStorageImpl createOrGetFromDisk() throws IOException, ClassNotFoundException {
        File dataFile = new File(DATA_FILE_NAME);
        if (dataFile.exists()) {
            try (FileInputStream fileInputStream = new FileInputStream(dataFile);
                 ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)
            ) {
                return (TorrentClientStorageImpl) objectInputStream.readObject();
            }
        } else {
            return new TorrentClientStorageImpl();
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
     * Returns all available for loading numbers of parts of file.
     *
     * @param fileId file's id
     * @return all available for loading numbers of parts of file
     */
    @NotNull
    @Override
    public List<Integer> getStat(long fileId) {
        FileInfoWithPath fileInfoWithPath = completedFiles.get(fileId);
        if (fileInfoWithPath != null) {
            return getAvailablePartIdList(
                    getPartCount(fileInfoWithPath.getFileInfo()));
        }

        IncompleteFileInfo incompleteFileInfo = uncompletedFiles.get(fileId);
        if (incompleteFileInfo != null) {
            return getAvailablePartIdList(incompleteFileInfo);
        }

        return Collections.emptyList();
    }

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
    @Override
    public FilePart getPart(long fileId, int partNum)
            throws NoSuchPartException, NoSuchFileException {


        FileInfoWithPath fileInfoWithPath = completedFiles.get(fileId);
        if (fileInfoWithPath != null) {
            return createFilePart(
                    fileInfoWithPath.getFilePath(),
                    fileInfoWithPath.getFileInfo(),
                    partNum
            );
        }

        IncompleteFileInfo incompleteFileInfo = uncompletedFiles.get(fileId);
        if (incompleteFileInfo != null) {
            @NotNull boolean[] partsStatus =
                    incompleteFileInfo.getPartsStatus();
            if (partsStatus.length > partNum) {
                return createFilePart(
                        incompleteFileInfo.getFilePath(),
                        incompleteFileInfo.getFileInfo(),
                        partNum
                );

            } else {
                throw new NoSuchPartException(partNum, fileId);
            }
        }

        throw new NoSuchFileException(fileId);
    }

    @Override
    public boolean contains(long fileId) {
        return completedFiles.containsKey(fileId)
                || uncompletedFiles.containsKey(fileId);
    }

    /**
     * Returns numbers of all available parts of the file.
     *
     * @param fileInfo meta-info of partly available file
     * @return numbers of all available parts
     */
    @NotNull
    private List<Integer> getAvailablePartIdList(
            @NotNull final IncompleteFileInfo fileInfo) {

        List<Integer> partIdList = new ArrayList<>(
                fileInfo.getCompletePartCount().get());
        @NotNull boolean[] partsStatus = fileInfo.getPartsStatus();
        for (int i = 0; i < partsStatus.length; i++) {
            if (partsStatus[i]) {
                partIdList.add(i);
            }
        }
        return partIdList;
    }

    /**
     * Return numbers of all parts in file.
     * Or in other words list of integer from 0 until {@code partCount}
     *
     * @param partCount count of part in file
     * @return numbers of all parts
     */
    @NotNull
    private List<Integer> getAvailablePartIdList(final int partCount) {
        ArrayList<Integer> partIdList = new ArrayList<>(partCount);
        for (int i = 0; i < partCount; i++) {
            partIdList.add(i);
        }
        return partIdList;
    }

    /**
     * Create meta-info about part of file.
     *
     * @param filePath path to file
     * @param fileInfo meta-info about file
     * @param partNum  number of part
     * @return meta-info about part of file
     * @throws NoSuchFileException if file doesn't exist
     */
    @NotNull
    private FilePart createFilePart(@NotNull final String filePath,
                                    @NotNull final FileInfo fileInfo,
                                    final int partNum)
            throws NoSuchFileException {
        Path path = Paths.get(filePath);
        File file = path.toFile();
        if (!file.exists() || file.isDirectory()) {
            throw new NoSuchFileException(fileInfo.getId());
        }

        long offset = partNum * PART_SIZE;
        int length = Math.min(PART_SIZE, (int) (fileInfo.getSize() - offset));

        return new FilePart(path, offset, length);
    }

    /**
     * целых чисел
     * Calculate count of part in file.
     *
     * @param fileInfo meta-info about file
     * @return count of part in file
     */
    private int getPartCount(@NotNull final FileInfo fileInfo) {
        return (int) ((fileInfo.getSize() + PART_SIZE - 1) / PART_SIZE);
    }

    /**
     * Returns all files' id which available for downloading from
     * this client (partly or completely).
     *
     * @return files' id which available for downloading from this client
     */
    @NotNull
    @Override
    public List<Long> getFilesIdList() {
        ArrayList<Long> fileIds = new ArrayList<>(
                completedFiles.size() + uncompletedFiles.size());
        fileIds.addAll(completedFiles.keySet());
        fileIds.addAll(uncompletedFiles.entrySet().stream()
                .filter(uncompletedFile ->
                        uncompletedFile.getValue().completePartCount.get() > 0)
                .map(Map.Entry::getKey).collect(Collectors.toList()));
        return fileIds;
    }

    /**
     * Returns all files' id which has incompletely not loaded parts.
     *
     * @return files' id which has incompletely not loaded parts
     */
    @Override
    public @NotNull List<Long> getIncompleteFileIds() {
        return new ArrayList<>(uncompletedFiles.keySet());
    }

    /**
     * Returns all numbers of parts of file which isn't loaded.
     *
     * @param fileId id of file
     * @return numbers of parts which isn't loaded
     */
    @Override
    public @NotNull List<Integer> getIncompleteParts(long fileId) {
        IncompleteFileInfo incompleteFileInfo = uncompletedFiles.get(fileId);
        if (incompleteFileInfo != null) {
            List<Integer> partNums = new ArrayList<>(
                    incompleteFileInfo.getPartsStatus().length
                            - incompleteFileInfo.getCompletePartCount().get());
            for (int i = 0;
                 i < incompleteFileInfo.getPartsStatus().length; i++) {
                if (!incompleteFileInfo.getPartsStatus()[i]) {
                    partNums.add(i);
                }
            }
            return partNums;
        }
        return new ArrayList<>();
    }

    /**
     * Mark part of file as loaded.
     * If it doesn't have such part or file will not throw any exceptions.
     * Warning! no more than one thread can load one part.
     *
     * @param fileInfo file's meta-info
     * @param partNum  number of part in file
     * @see FileInfo
     */
    @Override
    public void uploadPart(@NotNull FileInfo fileInfo, int partNum) {
        uncompletedFiles.computeIfPresent(fileInfo.getId(), (key, info) -> {
            if (info.getPartsStatus().length > partNum
                    && !info.getPartsStatus()[partNum]) {
                info.getPartsStatus()[partNum] = true;
                int completeCount =
                        info.getCompletePartCount().incrementAndGet();
                if (completeCount == info.getPartsStatus().length) {
                    completedFiles.put(
                            info.getFileInfo().getId(),
                            new FileInfoWithPath(info.getFileInfo(),
                                    info.getFilePath())
                    );
                    return null;
                }
            }
            return info;
        });

    }

    /**
     * Add not loaded file.
     * All parts of file will be mark as not loaded.
     *
     * @param filePath path to file.
     * @param fileInfo file's meta-info
     * @see FileInfo
     */
    @Override
    public void addNotLoadedFile(@NotNull Path filePath,
                                 @NotNull FileInfo fileInfo) {
        uncompletedFiles.putIfAbsent(fileInfo.getId(),
                new IncompleteFileInfo(fileInfo,
                        filePath.toString(),
                        new boolean[getPartCount(fileInfo)])
        );
    }

    /**
     * Add file already existing on disk.
     * All parts of file will be mark as loaded.
     *
     * @param filePath path to file.
     * @param fileInfo file's meta-info.
     * @see FileInfo
     */
    @Override
    public void addExistingFile(@NotNull Path filePath,
                                @NotNull FileInfo fileInfo) {
        completedFiles.put(fileInfo.getId(),
                new FileInfoWithPath(fileInfo, filePath.toString()));
    }


    /**
     * This class stores info about fully available file and path on disc.
     */
    @Data
    private final static class FileInfoWithPath implements Serializable {
        /**
         * Meta info about file
         */
        @NotNull
        private final FileInfo fileInfo;
        /**
         * Path to file on disk
         */
        @NotNull
        private final String filePath;
    }

    /**
     * This class stores info about partly available file and path on disk.
     */
    @Data
    private final static class IncompleteFileInfo implements Serializable {
        /**
         * Meta info about file
         */
        @NotNull
        private final FileInfo fileInfo;
        /**
         * Path to file on disk
         */
        @NotNull
        private final String filePath;
        /**
         * Status of parts.
         * i-th elements is true if i-th part of file is available,
         * false otherwise.
         */
        @NotNull
        private final boolean[] partsStatus;
        /**
         * Count of available parts in file
         */
        @NotNull
        private final AtomicInteger completePartCount = new AtomicInteger(0);
    }
}
