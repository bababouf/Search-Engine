package servlets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.http.HttpServletRequest;

import java.io.BufferedReader;
import java.io.IOException;


public class ServletUtilities {

    public static JsonObject parseRequestBody(HttpServletRequest request) throws IOException {
        /*
            BufferedReader and Strinbuilder are essential classes for reading data efficiently.
            BufferedReader is used for reading text from a character-input stream, buffering
            characters so as to provide for efficient reading of chars, arrays, and lines.
            It is commonly used to read text from files.
            It provides methods like read(), readLine(), and ready() for reading chars or lines from
            the input stream.

            Stringbuilder is used to create mutable sequences of chars. It's similar to string in c++
            but is mutable.
            It provides methods to append, insert, or delete chars in the sequence.
            It's particularly useful when needing to build strings dynamically, especially when
            you're reading and concatenating multiple strings.
            Unlike String, which is immutable in Java (once created you cant modify its content),
            Strinbuilder allows you to modify the sequence of chars it holds without creating
            new objects.
         */

        BufferedReader reader = request.getReader();

        StringBuilder requestBody = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            requestBody.append(line);
        }
        return JsonParser.parseString(requestBody.toString()).getAsJsonObject();
    }


}