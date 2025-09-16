package vio.tabitomo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/tripselect/step1")
    public String tripselectStep1() {
        return "tripselect/step1";
    }

    @GetMapping("/menu")
    public String menu() {
        return "menu";
    }
}
