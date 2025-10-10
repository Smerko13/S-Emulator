package servlets;

import engine.Program;
import engine.S_Emulator; // Your engine class
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
        byte[] requestBytes = req.getInputStream().readAllBytes();
        String xmlContent = new String(requestBytes, StandardCharsets.UTF_8);

        System.out.println("SERVER - Received bytes: " + requestBytes.length);
        System.out.println("SERVER - XML length: " + xmlContent.length());
        System.out.println("SERVER - First 200 chars: " + xmlContent.substring(0, Math.min(200, xmlContent.length())));

        String userId = req.getParameter("userId");
        System.out.println("SERVER - User ID: " + userId);

        ServerContext context = ServerContext.getInstance();
        S_Emulator userEngine = new Program(true);

        // Add detailed validation debugging
        System.out.println("SERVER - About to call readProgramFromXml...");

        try {
            boolean isValid = userEngine.readProgramFromXml(xmlContent);
            System.out.println("SERVER - Validation result: " + isValid);

            if (isValid) {
                context.storeUserProgram(userId, userEngine);
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write("XML is valid and program stored");
                System.out.println("SERVER - Success: XML validated and stored");
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("XML validation failed");
                System.out.println("SERVER - Failed: XML validation returned false");
            }
        } catch (Exception e) {
            System.out.println("SERVER - Exception during validation: " + e.getMessage());
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Validation error: " + e.getMessage());
        }
    }
}