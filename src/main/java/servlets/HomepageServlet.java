package servlets;

import java.io.*;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet(name = "HomepageServlet", value = "/home")
public class HomepageServlet extends HttpServlet {
    private String message;

    public void init() {
        message = "Hello World!";
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        getServletContext().getRequestDispatcher("/frontEnd/index.html").forward(request, response);

        ServletContext context = getServletContext();

        // Set a global variable
        context.setAttribute("directory", "default");

    }

    public void destroy() {
    }

}