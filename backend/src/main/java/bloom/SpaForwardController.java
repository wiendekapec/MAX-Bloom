package bloom;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaForwardController {

    @GetMapping(value = {
            "/",
            "/dashboard",
            "/catalog",
            "/community",
            "/checkout",
            "/waiting",
            "/success",
            "/payment-error",
            "/my-subscriptions",
            "/new-plan"
    })
    public String forwardSpa() {
        return "forward:/index.html";
    }
}
