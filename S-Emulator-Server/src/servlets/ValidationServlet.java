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
        String programName = req.getParameter("programName");
        System.out.println("SERVER - User ID: " + userId);
        System.out.println("SERVER - Program Name: " + programName);

        ServerContext context = ServerContext.getInstance();
        S_Emulator userEngine = new Program(true);

        // Add detailed validation debugging
        System.out.println("SERVER - About to call readProgramFromXml...");

        try {
            boolean isValid = userEngine.readProgramFromXml(xmlContent);
            System.out.println("SERVER - Validation result: " + isValid);

            if (isValid) {
                // Extract program name from XML if not provided as parameter
                if (programName == null || programName.trim().isEmpty()) {
                    programName = extractProgramNameFromXml(xmlContent);
                }

                context.storeUserProgram(userId, programName, userEngine);
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write("XML is valid and program stored with name: " + programName);
                System.out.println("SERVER - Success: XML validated and stored for program: " + programName);
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

    private String extractProgramNameFromXml(String xmlContent) {
        try {
            // Look for name attribute in S-Program tag
            int nameStart = xmlContent.indexOf("name=\"");
            if (nameStart != -1) {
                nameStart += 6; // Skip 'name="'
                int nameEnd = xmlContent.indexOf("\"", nameStart);
                if (nameEnd != -1) {
                    return xmlContent.substring(nameStart, nameEnd);
                }
            }

            // Fallback: generate a name based on timestamp
            return "Program_" + System.currentTimeMillis();
        } catch (Exception e) {
            return "Unknown_Program_" + System.currentTimeMillis();
        }
    }
}