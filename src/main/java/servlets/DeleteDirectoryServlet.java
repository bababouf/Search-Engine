package servlets;

import com.azure.storage.blob.BlobContainerClient;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import modules.database.PostgresDB;
import modules.indexing.AzureBlobStorageClient;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "DeleteDirectoryServlet", value = "/deleteDirectory")

public class DeleteDirectoryServlet extends HttpServlet
{

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        System.out.println("In the delete directory servlet");
        JsonObject jsonBody = ServletUtilities.parseRequestBody(request);
        String containerName = jsonBody.get("containerName").getAsString();

        deleteAzureStorageFiles(containerName);
        deleteDirectoryDatabaseTable(containerName);

        List<ServletUtilities.Directory> userDirectories = ServletUtilities.getUserDirectories(containerName);
        ServletContext context = getServletContext();
        context.setAttribute("userDirectories", userDirectories);
    }

    public void deleteAzureStorageFiles(String containerName){
        AzureBlobStorageClient client = new AzureBlobStorageClient(containerName);
        BlobContainerClient containerClient = client.getContainerClient();
        containerClient.deleteIfExists();
    }
    public void deleteDirectoryDatabaseTable(String containerName) {
        PostgresDB database = new PostgresDB("user_directory");
        database.setTableName(containerName);
        database.dropTable();
    }

}