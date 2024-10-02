package modules.decompression;

import org.apache.http.HttpEntity;
import org.apache.http.entity.HttpEntityWrapper;
import org.brotli.dec.BrotliInputStream;

import java.io.IOException;
import java.io.InputStream;

public class BrotliDecompressingEntity extends HttpEntityWrapper
{
    public BrotliDecompressingEntity(HttpEntity entity) {
        super(entity);
    }

    @Override
    public InputStream getContent() throws IOException
    {
        return new BrotliInputStream(wrappedEntity.getContent());
    }

    @Override
    public boolean isRepeatable() {
        return wrappedEntity.isRepeatable();
    }

    @Override
    public long getContentLength() {
        return -1; // Brotli size is not defined
    }
}