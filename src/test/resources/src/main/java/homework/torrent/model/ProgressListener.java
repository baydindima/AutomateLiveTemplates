package homework.torrent.model;

/**
 * Listen progress of file downloading.
 */
public class ProgressListener {
    /**
     * Length of file in bytes.
     */
    private final long fileLength;
    /**
     * Loaded byte count.
     */
    private long loadedCount;

    /**
     * Listen progress of file downloading.
     *
     * @param fileLength length of file
     */
    public ProgressListener(final long fileLength) {
        this.fileLength = fileLength;
    }

    /**
     * Add to loaded byte count.
     *
     * @param byteCount count of loaded bytes
     */
    public synchronized void load(final long byteCount) {
        loadedCount += byteCount;
    }

    /**
     * Return current progress of loading.
     * A number from 0 to 1.
     *
     * @return current progress of loading.
     */
    public int getCurrentProgress() {
        return (int) ((loadedCount * 100) / fileLength);
    }

}
