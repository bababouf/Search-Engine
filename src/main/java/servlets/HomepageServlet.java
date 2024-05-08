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
        System.out.println("In The Homepage Servlet.");
        getServletContext().getRequestDispatcher("/frontEnd/index.html").forward(request, response);
        setDefaultPath();
    }

    public void setDefaultPath() {
        ServletContext context = getServletContext();
        context.setAttribute("directory", "default");
        context.setAttribute("path", "C://Users//agreg//Desktop//Copy of Project//search-engine//all-nps-sites-extracted");
    }

}