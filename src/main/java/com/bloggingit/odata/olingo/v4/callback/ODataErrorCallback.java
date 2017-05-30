package com.bloggingit.odata.olingo.v4.callback;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.olingo.server.api.ODataContentWriteErrorCallback;
import org.apache.olingo.server.api.ODataContentWriteErrorContext;

/**
 *
 * @author mes
 */
public class ODataErrorCallback implements ODataContentWriteErrorCallback {

    private static final Logger LOG = Logger.getLogger(ODataErrorCallback.class.getName());
    @Override
    public void handleError(ODataContentWriteErrorContext context, WritableByteChannel channel) {
        LOG.log(Level.SEVERE, "{0}:{1}", new Object[]{context.getException().getClass().getName(), context.getException().getMessage()});
        String message = "An error occurred with message: ";
        if (context.getException() != null) {
            message += context.getException().getMessage();
        }
        try {
            channel.write(ByteBuffer.wrap(message.getBytes()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
