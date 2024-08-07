package modules.indexing;

import com.azure.storage.blob.*;
import com.azure.storage.blob.models.BlobContainerItem;
import com.azure.storage.blob.models.BlobItem;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;


public class AzureBlobStorageClient {

    private final BlobServiceClient serviceClient;
    private BlobContainerClient containerClient;


    public AzureBlobStorageClient() {
        String connectionString = System.getenv("AZURE_STORAGE_CONNECTION_STRING");
        this.serviceClient = new BlobServiceClientBuilder().connectionString(connectionString).buildClient();
    }
    public AzureBlobStorageClient(String containerName)
    {
        String connectionString = System.getenv("AZURE_STORAGE_CONNECTION_STRING");
        this.serviceClient = new BlobServiceClientBuilder().connectionString(connectionString).buildClient();
        this.containerClient = this.serviceClient.getBlobContainerClient(containerName);
    }

    public BlobContainerClient createContainer(String containerName){
        BlobContainerClient client = serviceClient.getBlobContainerClient(containerName);

        if(client.exists()){
            this.containerClient = null;
        }
        else
        {
            this.containerClient = serviceClient.createBlobContainerIfNotExists(containerName);
        }
        return containerClient;
    }

    public BlobContainerClient getContainerClient()
    {
        return containerClient;
    }


    // Uploads data that is passed to the method to a specific blob
    public void uploadFile(String blobFileName, byte[] data)
    {
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

    public List<String> listContainers() {
        List<String> containerNames = new ArrayList<>();

        // List all containers in the storage account
        for (BlobContainerItem containerItem : serviceClient.listBlobContainers()) {
            containerNames.add(containerItem.getName());
        }

        return containerNames;
    }


}