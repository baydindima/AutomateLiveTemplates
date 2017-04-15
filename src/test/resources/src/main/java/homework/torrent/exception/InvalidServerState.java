package homework.torrent.exception;

import org.jetbrains.annotations.NonNls;

/**
 * Thrown when server has invalid state.
 */
public class InvalidServerState extends Exception {
    /**
     * Thrown when server has invalid state.
     */
    public InvalidServerState(@NonNls String message) {
        super(message);
    }
}
