package homework.torrent.model;

import homework.torrent.model.writer.ObjectWriter;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for serializable objects.
 */
public interface SerializableObject {
    /**
     * Method for writer of object.
     *
     * @return writer of object.
     */
    @NotNull ObjectWriter getWriter();
}
