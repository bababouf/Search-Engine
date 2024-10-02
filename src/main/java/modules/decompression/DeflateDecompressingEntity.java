package modules.decompression;

import org.apache.http.HttpEntity;
import org.apache.http.entity.HttpEntityWrapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.InflaterInputStream;

public class DeflateDecompressingEntity extends HttpEntityWrapper
{
    public DeflateDecompressingEntity(HttpEntity entity) {
        super(entity);
    }

    @Override
    public InputStream getContent() throws IOException
    {
        return new InflaterInputStream(wrappedEntity.getContent());
    }

    @Override
    public boolean isRepeatable() {
        return wrappedEntity.isRepeatable();
    }

    @Override
    public long getContentLength() {
        return -1; // Deflate size is not defined
    }
}
