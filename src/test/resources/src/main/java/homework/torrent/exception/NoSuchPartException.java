package homework.torrent.exception;

/**
 * Thrown if no such part.
 */
public class NoSuchPartException extends Exception {
    /**
     * Thrown if no such part.
     */
    public NoSuchPartException(final int partId, final long fileId) {
        super(String.format(
                "No such part of file exception. partId = %d, fileId = %d",
                partId,
                fileId)
        );
    }
}
