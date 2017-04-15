package homework.torrent.exception;

import org.jetbrains.annotations.NonNls;

/**
 * Thrown when query's format is invalid.
 *
 * @author dmitriy
 */
public class InvalidQueryFormat extends RuntimeException {

    /**
     * Thrown when query's format is invalid.
     *
     * @param message message of exception
     */
    public InvalidQueryFormat(@NonNls final String message) {
        super(message);
    }
}
