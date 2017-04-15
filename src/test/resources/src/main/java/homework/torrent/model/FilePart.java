package homework.torrent.model;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * Info about part of file.
 */
@Data
public class FilePart {
    /**
     * Dir path to file.
     */
    @NotNull
    private final Path filePath;
    /**
     * Offset from begin of file in bytes.
     */
    private final long offset;
    /**
     * Length of part in bytes.
     */
    private final int length;
}
