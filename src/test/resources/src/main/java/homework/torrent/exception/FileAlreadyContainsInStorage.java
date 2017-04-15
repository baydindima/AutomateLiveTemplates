package homework.torrent.exception;

/**
 * Thrown if file has already been added
 */
public class FileAlreadyContainsInStorage extends Exception {
    /**
     * Thrown if file has added in storage and
     */
    public FileAlreadyContainsInStorage() {
        super("file has already been added");
    }
}
