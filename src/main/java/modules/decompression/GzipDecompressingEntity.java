package modules.decompression;

import org.apache.http.HttpEntity;
import org.apache.http.entity.HttpEntityWrapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

public class GzipDecompressingEntity extends HttpEntityWrapper
{
    public GzipDecompressingEntity(HttpEntity entity) {
        super(entity);
    }

    @Override
    public InputStream getContent() throws IOException
    {
        return new GZIPInputStream(wrappedEntity.getContent());
    }

    @Override
    public boolean isRepeatable() {
        return wrappedEntity.isRepeatable();
    }

    @Override
    public long getContentLength() {
        return -1; // Gzip size is not defined
    }
}