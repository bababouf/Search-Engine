package servlets;

import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet(name = "RankedSearchServlet", value = "/search/rankedsearch")
public class RankedSearchServlet extends HttpServlet {
    private String message;

    public void init() {
        message = "Hello World!";
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("Im in the ranked servlet ooooooooooooooaaaaaaaass");
    }

    public void destroy() {
    }
}