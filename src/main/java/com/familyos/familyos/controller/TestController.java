package com.familyos.familyos.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/t1")
    public String t1() {
        return "Hello World";
    }
}
