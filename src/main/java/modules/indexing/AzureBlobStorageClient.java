package modules.indexing;

import com.azure.storage.blob.*;
import com.azure.storage.blob.models.BlobContainerItem;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * This class contains methods that connect and interact with the Azure Blob Storage account containing all the
 * indexing files. This includes uploading, downloading, and listing the containers associated with the Azure Storage
 * account.
 */
public class AzureBlobStorageClient {

    private final BlobServiceClient serviceClient;
    private BlobContainerClient containerClient;


    /**
     * Constructor that simply using an environment variable to connect to the storage account. With this constructor,
     * no specific container within the account is connected.
     */
    public AzureBlobStorageClient()
    {
        String connectionString = System.getenv("AZURE_STORAGE_CONNECTION_STRING");
        this.serviceClient = new BlobServiceClientBuilder().connectionString(connectionString).buildClient();
    }

    /**
     * Constructor that connects to the storage account, AND connects to a specific container within the account. This is
     * used to access the files of a specific corpus that is uploaded.
     */
    public AzureBlobStorageClient(String containerName)
    {
        String connectionString = System.getenv("AZURE_STORAGE_CONNECTION_STRING");
        this.serviceClient = new BlobServiceClientBuilder().connectionString(connectionString).buildClient();

        this.containerClient = this.serviceClient.getBlobContainerClient(containerName);
        if(!this.containerClient.exists())
        {
            this.containerClient = serviceClient.createBlobContainer(containerName);
        }
    }

    
    // Returns the container client
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
    public byte[] downloadFile(String blobFileName)
    {
        BlobClient blobClient = containerClient.getBlobClient(blobFileName);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        blobClient.download(outputStream);
        return outputStream.toByteArray();
    }

    // Lists all the containers associated with the storage account
    public List<String> listContainers() {
        List<String> containerNames = new ArrayList<>();

        // Add each container name to a list
        for (BlobContainerItem containerItem : serviceClient.listBlobContainers())
        {
            containerNames.add(containerItem.getName());
        }

        return containerNames;
    }


}