package bloom;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Контроллер перенаправления SPA-маршрутов на index.html.
 */
@Controller
public class SpaForwardController {

    @GetMapping(value = {
        "/{path:[^\\.]*}",
        "/catalog/**",
        "/business/**",
        "/subscriptions/**",
        "/community/**",
        "/payment/**"
    })
    public String forwardSpa() {
        return "forward:/index.html";
    }
}
