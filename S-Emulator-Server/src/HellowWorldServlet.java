import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;

import java.io.IOException;

@WebServlet(name = "Hello Servlet", urlPatterns = {"/hello"})
public class HellowWorldServlet extends HttpServlet {

    @Override
    protected void doGet(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp) throws IOException {
        resp.getWriter().write("Hello, World!");
    }
}
