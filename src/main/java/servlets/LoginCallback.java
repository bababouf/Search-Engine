package servlets;

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
import org.apache.commons.codec.binary.Base32;
import java.util.Collections;
import java.util.List;

/*
    The main purpose of this servlet is to verify the token it receives from the browser as per the Google documentation
    found here: https://developers.google.com/identity/gsi/web/guides/verify-google-id-token.

    Once the token is verified, several pieces of information are saved in an HTTPSession object (which will be used to
    display user information on the profile page). The HTTPSession object can be accessed across servlets, and contains
    user information including first name, profile picture, and a unique ID that differentiates users.

     Since the unique ID is over 250 characters long, it is hashed and encoded in Base32 to shorten it down. Lastly, in order
     to display user directories (directories a user has previously uploaded on their account) a method getUserDirectories(hashedID)
     is called. Each of these pieces of user information is saved in separate HTTPSession
     variables.
     */

@WebServlet(name = "LoginCallback", value = "/oauth2callback")
public class LoginCallback extends HttpServlet {

    private static final String CLIENT_ID = "529467941335-rrllroamg3ebfvgp9n9i8qeni04tguca.apps.googleusercontent.com";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // Get the ID token parameter from the request body
        String idTokenString = request.getParameter("id_token");

        if (idTokenString != null && !idTokenString.isEmpty())
        {
            try
            {
                // Verify the ID token (https://developers.google.com/identity/gsi/web/guides/verify-google-id-token)
                GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                        new NetHttpTransport(),
                        JacksonFactory.getDefaultInstance())
                        .setAudience(Collections.singletonList(CLIENT_ID))
                        .build();

                GoogleIdToken idToken = verifier.verify(idTokenString);

                if (idToken != null)
                {
                    GoogleIdToken.Payload payload = idToken.getPayload();

                    // Obtain the unique user id
                    String userId = payload.getSubject();

                    // Hash the id and encode with base32
                    String hashedID = hashAndEncodeID(userId);

                    // Obtain the directories associated with the user's ID
                    List<ServletUtilities.Directory> userDirectories = ServletUtilities.getUserDirectories(hashedID);

                    // Get basic user information to display on profile page
                    String firstName = (String) payload.get("given_name");
                    String profileURL = (String) payload.get("picture");

                    // Store user information in HTTPSession variables
                    request.getSession().setAttribute("firstName", firstName);
                    request.getSession().setAttribute("profileURL", profileURL);
                    request.getSession().setAttribute("uniqueID", hashedID);
                    request.getSession().setAttribute("userDirectories", userDirectories);

                    // If this point was reached, the token has been verified and the response status can be set to 500
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().write("Login successful");
                }
                else
                {
                    // Failed to verify token or something else happened
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

    // Hashes the token id obtained after verification, and then encodes with base 32
    public String hashAndEncodeID(String uniqueID) {
        try
        {
            // Hash the user ID using SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(uniqueID.getBytes(StandardCharsets.UTF_8));

            // Truncate the hash to 16 bytes
            byte[] truncatedHash = new byte[16];
            System.arraycopy(hash, 0, truncatedHash, 0, 16);

            // Base32 encode the truncated hash
            Base32 base32 = new Base32();
            String encoded = base32.encodeAsString(truncatedHash).toLowerCase();

            // Remove any padding characters if present
            encoded = encoded.replaceAll("=", "");

            return encoded;
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException(e);
        }
    }

}