package com.ican.saf.ui.dashboard.bff.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SPAFrontendController {
    @GetMapping({"/home", "/redirect-auth-login"})
    public String forwardToFrontend(){
        return "forward:/index.html";
    }
}
