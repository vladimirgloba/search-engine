package com.globa.search.engine.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DefaultController {


    @GetMapping("/admin")
    public String index() {
        return "index";
    }

}
