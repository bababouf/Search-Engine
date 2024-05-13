package servlets;

import java.io.*;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet(name = "HomepageServlet", value = "/home")
public class HomepageServlet extends HttpServlet {

    @Override
    public void init() throws ServletException {
        System.out.println("hola");
    }
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        System.out.println("In the homepage!!!!");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        getServletContext().getRequestDispatcher("/frontEnd/index.html").forward(request, response);
        setDefaultPath();
    }

    public void setDefaultPath() {
        ServletContext context = getServletContext();
        String servletContextDir = context.getRealPath("/");
        String projectRoot = ServletUtilities.getProjectRootDir(servletContextDir);
        String defaultDirectoryPath = projectRoot + File.separator + "all-nps-sites-extracted";
        context.setAttribute("path", defaultDirectoryPath);
    }

}