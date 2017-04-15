package homework.torrent.model.writer;

import java.nio.ByteBuffer;

/**
 * Created by Dmitriy Baidin.
 */
public class ShortWriter extends AbstractObjectWriter {

    public ShortWriter(final short value) {
        super(ByteBuffer.allocate(Short.BYTES).putShort(value).array());
    }
}
