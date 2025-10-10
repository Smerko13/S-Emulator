package servlets;


import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@WebServlet("/validate")
public class ValidationServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String xmlContent = new String(req.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        // Process xmlContent as needed (e.g., validate, parse)
        resp.setStatus(HttpServletResponse.SC_OK);
    }
}
