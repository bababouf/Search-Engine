package servlets;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@MultipartConfig
@WebServlet(name = "UploadDirServlet", value = "/upload")
public class UploadDirServlet extends HttpServlet {


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

       System.out.println("Hi");
       createActuallyNothing();

    }

    public void createActuallyNothing(){
        System.out.println("Creating nothing and doing nothing. Nothing");
    }


}