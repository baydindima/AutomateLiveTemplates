package homework.torrent.model.writer;


import org.jetbrains.annotations.NotNull;

/**
 * ObjectWriter for string data.
 */
class StringWithoutSizeWriter extends AbstractObjectWriter {
    /**
     * Create new instance of string writer.
     *
     * @param string inner string to write
     */
    StringWithoutSizeWriter(@NotNull final String string) {
        super(string.getBytes());
    }

}
