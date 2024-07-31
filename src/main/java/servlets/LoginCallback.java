package servlets;


import com.azure.storage.blob.models.BlobItem;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import modules.indexing.AzureBlobStorageClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

@WebServlet(name = "LoginCallback", value = "/oauth2callback")
public class LoginCallback extends HttpServlet {

    private static final String CLIENT_ID = "529467941335-rrllroamg3ebfvgp9n9i8qeni04tguca.apps.googleusercontent.com";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String idTokenString = request.getParameter("id_token");

        if (idTokenString != null && !idTokenString.isEmpty())
        {
            try
            {
                // Verify the ID token
                GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                        new NetHttpTransport(),
                        JacksonFactory.getDefaultInstance())
                        .setAudience(Collections.singletonList(CLIENT_ID))
                        .build();

                GoogleIdToken idToken = verifier.verify(idTokenString);

                if (idToken != null)
                {
                    GoogleIdToken.Payload payload = idToken.getPayload();
                    String userId = payload.getSubject();
                    String hashedID = base64Hash(userId);

                    AzureBlobStorageClient client = new AzureBlobStorageClient("user-uploaded-directories");
                    List<String> userDirectories = client.getUserDirectories(hashedID);

                    System.out.println("unique ID: " + hashedID);
                    String firstName = (String) payload.get("given_name");
                    String profileURL = (String) payload.get("picture");
                    System.out.println("profileURL: " + profileURL);

                    // Store user information in session or database
                    request.getSession().setAttribute("firstName", firstName);
                    request.getSession().setAttribute("profileURL", profileURL);
                    request.getSession().setAttribute("uniqueID", hashedID);
                    request.getSession().setAttribute("userDirectories", userDirectories);



                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().write("Login successful");
                }
                else
                {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid ID token");
                }
            }
            catch (GeneralSecurityException e)
            {
                e.printStackTrace();
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to verify ID token");
            }
        }
        else
        {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No ID token parameter");
        }
    }

    public String base64Hash(String input){

        System.out.println("In the base64 hash");
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            // Encode the hash in Base64 to make it shorter
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        }
        catch (NoSuchAlgorithmException e)
        {
            System.out.println("We havin an issue.");
            throw new RuntimeException(e);
        }

    }

}