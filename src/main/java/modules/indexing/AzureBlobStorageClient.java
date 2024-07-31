package modules.indexing;

import com.azure.storage.blob.*;
import com.azure.storage.blob.models.BlobItem;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/*
This class is used to connect and obtain a client for accessing Azure Blob Storage
 */
public class AzureBlobStorageClient {

    private final BlobContainerClient containerClient;

    // Obtains the client after accessing an environment variable containing the connection string
    public AzureBlobStorageClient() {
        String connectionString = System.getenv("AZURE_STORAGE_CONNECTION_STRING");
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(connectionString).buildClient();

        /*
        After connecting to the storage account using the conn string, the following will connect to the specific container
        where the indexing files live
         */
        String containerName = "se-indexing-files";
        this.containerClient = blobServiceClient.getBlobContainerClient(containerName);
    }

    public AzureBlobStorageClient(String containerName) {
        String connectionString = System.getenv("AZURE_STORAGE_CONNECTION_STRING");
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(connectionString).buildClient();

        /*
        After connecting to the storage account using the conn string, the following will connect to the specific container
        where the indexing files live
         */

        this.containerClient = blobServiceClient.getBlobContainerClient(containerName);
    }

    // Uploads data that is passed to the method to a specific blob
    public void uploadFile(String blobFileName, byte[] data) {
        BlobClient blobClient = containerClient.getBlobClient(blobFileName);
        blobClient.upload(new ByteArrayInputStream(data), data.length, true);
    }

    // Downloads data from a specific blob, returning a byte array
    public byte[] downloadFile(String blobFileName) {
        BlobClient blobClient = containerClient.getBlobClient(blobFileName);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        blobClient.download(outputStream);
        return outputStream.toByteArray();
    }

    public List<String> getUserDirectories(String hashedID) {
        List<String> directories = new ArrayList<>();

        // List all blobs in the container
        for (BlobItem blobItem : containerClient.listBlobs()) {
            String blobName = blobItem.getName();

            // Check if the blob name starts with the hashed ID followed by "!"
            if (blobName.startsWith(hashedID + "!")) {
                // Extract the directory name part
                String directoryName = blobName.substring((hashedID + "!").length());
                directories.add(directoryName);
            }
        }

        return directories;
    }


}