//package com.ican.saf.ui.dashboard.bff.web.controller;
//
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.GetMapping;
//
//@Controller
//public class SPAFrontendController {
//    @GetMapping({"/home", "/redirect-auth-login"})
//    public String forwardToFrontend(){
//        return "forward:/index.html";
//    }
//
//
//    /**
//     * Matches any route that does NOT contain a file extension dot (.)
//     * This safely forwards browser navigation (/home, /profile, /settings/security)
//     * to Vue, while letting Spring serve your static assets normally.
//     */
//    @GetMapping(value = {
//            "/{path:[^\\.]*}",
//            "/**/{path:[^\\.]*}"
//    })
//    public String forwardToFrontend() {
//        return "forward:/index.html";
//    }
//}
