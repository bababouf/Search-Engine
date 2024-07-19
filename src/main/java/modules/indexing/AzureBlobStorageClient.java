package modules.indexing;
import com.azure.storage.blob.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class AzureBlobStorageClient {

    private final BlobContainerClient containerClient;

    public AzureBlobStorageClient() {
        String connectionString = System.getenv("AZURE_STORAGE_CONNECTION_STRING");
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(connectionString).buildClient();
        String containerName = "se-indexing-files";
        this.containerClient = blobServiceClient.getBlobContainerClient(containerName);
    }

    public void uploadFile(String blobFileName, byte[] data) {
        BlobClient blobClient = containerClient.getBlobClient(blobFileName);
        blobClient.upload(new ByteArrayInputStream(data), data.length, true);
    }

    public byte[] downloadFile(String blobFileName) {
        BlobClient blobClient = containerClient.getBlobClient(blobFileName);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        blobClient.download(outputStream);
        return outputStream.toByteArray();
    }


}