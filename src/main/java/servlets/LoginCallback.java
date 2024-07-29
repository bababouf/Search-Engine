package servlets;


import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@WebServlet(name = "LoginCallback", value = "/oauth2callback")
public class LoginCallback extends HttpServlet {

    private static final String CLIENT_ID = "529467941335-rrllroamg3ebfvgp9n9i8qeni04tguca.apps.googleusercontent.com";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {


        String idTokenString = request.getParameter("id_token");

        if (idTokenString != null && !idTokenString.isEmpty()) {
            try {
                // Verify the ID token
                GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                        new NetHttpTransport(),
                        JacksonFactory.getDefaultInstance())
                        .setAudience(Collections.singletonList(CLIENT_ID))
                        .build();

                GoogleIdToken idToken = verifier.verify(idTokenString);
                if (idToken != null) {
                    GoogleIdToken.Payload payload = idToken.getPayload();
                    String userId = payload.getSubject(); // User ID from Google
                    String userEmail = payload.getEmail(); // User email
                    System.out.println("userEmail: " + userEmail);

                    // Store user information in session or database
                    request.getSession().setAttribute("userId", userId);
                    request.getSession().setAttribute("userEmail", userEmail);

                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().write("Login successful");
                } else {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid ID token");
                }
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to verify ID token");
            }
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No ID token parameter");
        }
    }
}