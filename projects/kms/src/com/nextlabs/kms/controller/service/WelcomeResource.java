package com.nextlabs.kms.controller.service;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class WelcomeResource {
    @RequestMapping(value="/")
    public String index(){
        return "redirect:/index.html";
    }    
}