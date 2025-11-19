package com.combiphar.core; // Sesuaikan dengan package kamu

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/")
    public String sapa() {
        return "Halo Combiphar! Aplikasi Backend sudah jalan!";
    }
}