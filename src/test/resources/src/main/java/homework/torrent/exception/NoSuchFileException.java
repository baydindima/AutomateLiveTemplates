package homework.torrent.exception;

/**
 * Thrown if no such file.
 */
public class NoSuchFileException extends Exception {
    /**
     * Thrown if no such file.
     */
    public NoSuchFileException(final long fileId) {
        super(String.format("No such file. fileId = %d", fileId));
    }
}
