package root.configure;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/")
public class RedirectController {
    @RequestMapping("")
    public void toIndexPage(HttpServletResponse response) throws IOException {
        response.sendRedirect("index.html");
    }
}
