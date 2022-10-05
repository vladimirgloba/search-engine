package com.globa.search.engine.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class DefaultController {
@Autowired


    @GetMapping("/admin")

    public String index(){

        return "index";
    }

}
